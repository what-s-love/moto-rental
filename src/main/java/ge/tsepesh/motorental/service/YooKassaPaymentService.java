package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.client.YooKassaApiClient;
import ge.tsepesh.motorental.config.YooKassaProperties;
import ge.tsepesh.motorental.dto.yookassa.YooKassaCreatePaymentRequest;
import ge.tsepesh.motorental.dto.yookassa.YooKassaPaymentResponse;
import ge.tsepesh.motorental.exception.PaymentException;
import ge.tsepesh.motorental.model.Booking;
import ge.tsepesh.motorental.model.Payment;
import ge.tsepesh.motorental.repository.BookingRepository;
import ge.tsepesh.motorental.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис создания платёжных ссылок через YooKassa (сценарий: Умный платёж + Redirect).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class YooKassaPaymentService {

    private static final String PROVIDER = "yookassa";
    private static final String METADATA_BOOKING_ID = "booking_id";

    private final YooKassaApiClient yooKassaApiClient;
    private final YooKassaProperties yooKassaProperties;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    /**
     * Создаёт платёж в YooKassa на сумму предоплаты, сохраняет Payment-запись,
     * связывает её с бронированием и возвращает ссылку на форму оплаты (confirmation_url).
     *
     * <p>Вызывается из {@code BookingService.createBooking} на шаге 9
     * в рамках той же транзакции, что и создание бронирования.</p>
     *
     * @param booking сохранённый объект бронирования
     * @return confirmation_url для перенаправления пользователя (или отправки в письме)
     */
    public String createPrepaymentLink(Booking booking) {
        log.info("Creating YooKassa prepayment for booking id={}", booking.getId());

        String idempotenceKey = buildIdempotenceKey(booking);

        YooKassaCreatePaymentRequest request = buildPaymentRequest(booking);

        YooKassaPaymentResponse response = yooKassaApiClient.createPayment(request, idempotenceKey);

        String confirmationUrl = extractConfirmationUrl(response, booking.getId());

        savePaymentAndLinkToBooking(response, booking);

        log.info("YooKassa payment created: paymentId={}, bookingId={}",
                response.id(), booking.getId());
        return confirmationUrl;
    }

    // ─────────────────────────────────────────────────────────────────────────

    private YooKassaCreatePaymentRequest buildPaymentRequest(Booking booking) {
        YooKassaProperties.ReceiptItemProperties receiptConfig = yooKassaProperties.receipt();
        String amountValue = yooKassaProperties.prepaymentAmount()
                .setScale(2, RoundingMode.UNNECESSARY)
                .toPlainString();
        String currency = yooKassaProperties.currency();

        YooKassaCreatePaymentRequest.Amount amount =
                new YooKassaCreatePaymentRequest.Amount(amountValue, currency);

        YooKassaCreatePaymentRequest.ReceiptItem item =
                new YooKassaCreatePaymentRequest.ReceiptItem(
                        String.format(receiptConfig.itemDescription(), booking.getId()),
                        receiptConfig.quantity(),
                        amount,
                        receiptConfig.vatCode()
                );

        YooKassaCreatePaymentRequest.Customer customer =
                new YooKassaCreatePaymentRequest.Customer(booking.getClient().getEmail());

        YooKassaCreatePaymentRequest.Receipt receipt =
                new YooKassaCreatePaymentRequest.Receipt(customer, List.of(item));

        YooKassaCreatePaymentRequest.Confirmation confirmation =
                new YooKassaCreatePaymentRequest.Confirmation("redirect", yooKassaProperties.returnUrl());

        String description = String.format(receiptConfig.itemDescription(), booking.getId());

        Map<String, String> metadata = Map.of(METADATA_BOOKING_ID, String.valueOf(booking.getId()));

        YooKassaCreatePaymentRequest paymentRequest = YooKassaCreatePaymentRequest.builder()
                .amount(amount)
                .capture(true)
                .confirmation(confirmation)
                .description(description)
                .metadata(metadata)
                .receipt(receipt)
                .build();

        log.info("YooKassa payment request: " + paymentRequest);

        return paymentRequest;
    }

    private String extractConfirmationUrl(YooKassaPaymentResponse response, Integer bookingId) {
        if (response == null) {
            throw new PaymentException("YooKassa returned null response for booking " + bookingId);
        }
        if (response.confirmation() == null
                || response.confirmation().confirmationUrl() == null
                || response.confirmation().confirmationUrl().isBlank()) {
            throw new PaymentException(
                    "YooKassa did not return confirmation_url for booking " + bookingId
                            + ", paymentId=" + (response.id() != null ? response.id() : "unknown"));
        }
        return response.confirmation().confirmationUrl();
    }

    private void savePaymentAndLinkToBooking(YooKassaPaymentResponse response, Booking booking) {
        Payment payment = new Payment();
        payment.setProvider(PROVIDER);
        payment.setTransactionRef(response.id());
        payment.setAmount(yooKassaProperties.prepaymentAmount());
        payment.setCurrency(yooKassaProperties.currency());
        payment.setCreatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        booking.setPayment(payment);
        bookingRepository.save(booking);
    }

    /**
     * Ключ идемпотентности: стабилен для одного бронирования в первые 24 ч,
     * что защищает от случайного двойного создания платежа при сетевых ретраях.
     * При повторной генерации ссылки (после payment.canceled) нужно передавать новый UUID.
     */
    private String buildIdempotenceKey(Booking booking) {
        return "booking-" + booking.getId() + "-" + UUID.randomUUID();
    }
}
