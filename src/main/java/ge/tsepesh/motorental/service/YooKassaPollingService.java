package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.client.YooKassaApiClient;
import ge.tsepesh.motorental.dto.yookassa.YooKassaPaymentResponse;
import ge.tsepesh.motorental.enums.BookingStatus;
import ge.tsepesh.motorental.model.Booking;
import ge.tsepesh.motorental.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Заготовка polling-стратегии подтверждения платежей.
 *
 * <p>Polling — альтернатива webhook. Сервис периодически опрашивает YooKassa API
 * для всех бронирований в статусе {@link BookingStatus#PENDING_PAYMENT}, у которых
 * уже есть привязанный Payment с transactionRef.</p>
 *
 * <h3>Как активировать</h3>
 * <ol>
 *   <li>Раскомментируйте аннотацию {@code @Scheduled} на методе {@link #pollPendingPayments()}.</li>
 *   <li>Добавьте {@code @EnableScheduling} в конфигурационный класс или основной класс приложения.</li>
 * </ol>
 *
 * <h3>Когда использовать вместо webhook</h3>
 * <ul>
 *   <li>Нет публичного HTTPS-эндпоинта для приёма уведомлений (локальная разработка).</li>
 *   <li>Необходима дополнительная надёжность: polling как fallback к webhook.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class YooKassaPollingService {

    private final BookingRepository bookingRepository;
    private final YooKassaApiClient yooKassaApiClient;
    private final PaymentConfirmationService paymentConfirmationService;

    /**
     * Опрашивает статусы всех незавершённых платежей.
     *
     * <p>Активировать через {@code @Scheduled(fixedDelay = 60_000)}
     * или {@code @Scheduled(cron = "0 * * * * *")} — по минуте.</p>
     *
     * <p>Метод намеренно не помечен {@code @Scheduled}, чтобы не запускаться
     * пока webhook является основным каналом подтверждения.</p>
     */
    // @Scheduled(fixedDelayString = "${yookassa.polling.fixed-delay-ms:60000}")
    public void pollPendingPayments() {
        List<Booking> pending = bookingRepository.findByStatusWithPayment(BookingStatus.PENDING_PAYMENT);

        if (pending.isEmpty()) {
            log.debug("No PENDING_PAYMENT bookings to poll");
            return;
        }

        log.info("Polling {} PENDING_PAYMENT bookings...", pending.size());
        for (Booking booking : pending) {
            pollSingleBooking(booking);
        }
    }

    /**
     * Проверяет статус платежа для конкретного бронирования.
     * Можно вызвать вручную (например, из AdminController).
     */
    public void pollSingleBooking(Booking booking) {
        if (booking.getPayment() == null
                || booking.getPayment().getTransactionRef() == null) {
            log.debug("Booking {} has no payment ref, skipping poll", booking.getId());
            return;
        }

        String paymentId = booking.getPayment().getTransactionRef();
        try {
            YooKassaPaymentResponse response = yooKassaApiClient.getPayment(paymentId);
            paymentConfirmationService.processVerifiedPayment(response);
        } catch (Exception e) {
            log.error("Error polling payment {} for booking {}: {}",
                    paymentId, booking.getId(), e.getMessage(), e);
        }
    }
}
