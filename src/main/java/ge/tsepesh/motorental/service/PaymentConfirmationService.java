package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.config.YooKassaProperties;
import ge.tsepesh.motorental.dto.yookassa.YooKassaPaymentResponse;
import ge.tsepesh.motorental.enums.AppSettingKey;
import ge.tsepesh.motorental.enums.BookingStatus;
import ge.tsepesh.motorental.model.Booking;
import ge.tsepesh.motorental.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;

/**
 * Единая точка обработки результата платежа YooKassa.
 * Используется как webhook-обработчиком, так и polling-сервисом.
 *
 * <p>Метод {@link #processVerifiedPayment(YooKassaPaymentResponse)} принимает
 * <em>уже верифицированный</em> объект платежа, полученный напрямую из API
 * (GET /v3/payments/{id}), а не из тела webhook-уведомления.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConfirmationService {

    private static final String METADATA_BOOKING_ID = "booking_id";

    private final BookingRepository bookingRepository;
    private final YooKassaProperties yooKassaProperties;
    private final AppSettingService appSettingService;

    /**
     * Обрабатывает верифицированный объект платежа: переводит бронирование
     * в статус {@link BookingStatus#PAID} или {@link BookingStatus#PAYMENT_FAILED}.
     *
     * @param payment актуальный объект платежа из YooKassa API
     */
    @Transactional
    public void processVerifiedPayment(YooKassaPaymentResponse payment) {
        log.info("Processing verified payment: paymentId={}, status={}", payment.id(), payment.status());

        Integer bookingId = extractBookingId(payment);
        if (bookingId == null) {
            log.warn("No {} in payment metadata, skipping. paymentId={}", METADATA_BOOKING_ID, payment.id());
            return;
        }

        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            log.warn("Booking not found for id={}, paymentId={}", bookingId, payment.id());
            return;
        }
        Booking booking = bookingOpt.get();

        if (booking.getBookingStatus() == BookingStatus.PAID) {
            log.info("Booking {} is already PAID — skipping (idempotency guard)", bookingId);
            return;
        }

        if (!isPaymentBelongsToBooking(payment, booking)) {
            log.warn("Payment id mismatch: paymentId={} does not match booking.payment.transactionRef={}",
                    payment.id(),
                    booking.getPayment() != null ? booking.getPayment().getTransactionRef() : "null");
            return;
        }

        switch (payment.status()) {
            case "succeeded" -> handleSucceeded(payment, booking);
            case "canceled"  -> handleCanceled(payment, booking);
            default -> log.debug("Payment {} has intermediate status '{}', no action taken",
                    payment.id(), payment.status());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void handleSucceeded(YooKassaPaymentResponse payment, Booking booking) {
        if (!Boolean.TRUE.equals(payment.paid())) {
            log.warn("Payment {} status=succeeded but paid=false, skipping", payment.id());
            return;
        }
        if (!isAmountValid(payment)) {
            log.warn("Payment {} amount mismatch: expected={}, got={}",
                    payment.id(),
                    new BigDecimal(appSettingService.getValue(AppSettingKey.PREPAYMENT_AMOUNT))
                            .setScale(2, RoundingMode.UNNECESSARY),
                    payment.amount() != null ? payment.amount().value() : "null");
            return;
        }

        booking.setBookingStatus(BookingStatus.PAID);
        bookingRepository.save(booking);
        log.info("Booking {} marked as PAID (paymentId={})", booking.getId(), payment.id());
    }

    private void handleCanceled(YooKassaPaymentResponse payment, Booking booking) {
        booking.setBookingStatus(BookingStatus.PAYMENT_FAILED);
        bookingRepository.save(booking);
        log.info("Booking {} marked as PAYMENT_FAILED (paymentId={})", booking.getId(), payment.id());
    }

    private Integer extractBookingId(YooKassaPaymentResponse payment) {
        Map<String, String> metadata = payment.metadata();
        if (metadata == null || !metadata.containsKey(METADATA_BOOKING_ID)) {
            return null;
        }
        try {
            return Integer.valueOf(metadata.get(METADATA_BOOKING_ID));
        } catch (NumberFormatException e) {
            log.error("Invalid booking_id in payment metadata: '{}'", metadata.get(METADATA_BOOKING_ID));
            return null;
        }
    }

    private boolean isPaymentBelongsToBooking(YooKassaPaymentResponse payment, Booking booking) {
        return booking.getPayment() != null
                && payment.id().equals(booking.getPayment().getTransactionRef());
    }

    private boolean isAmountValid(YooKassaPaymentResponse payment) {
        if (payment.amount() == null || payment.amount().value() == null) {
            return false;
        }
        BigDecimal received = new BigDecimal(payment.amount().value());
        BigDecimal expected = new BigDecimal(appSettingService.getValue(AppSettingKey.PREPAYMENT_AMOUNT))
                .setScale(2, RoundingMode.UNNECESSARY);

        return received.compareTo(expected) == 0;
    }
}
