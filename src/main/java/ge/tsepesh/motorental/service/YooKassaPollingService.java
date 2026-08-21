package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.client.YooKassaApiClient;
import ge.tsepesh.motorental.dto.yookassa.YooKassaPaymentResponse;
import ge.tsepesh.motorental.enums.BookingStatus;
import ge.tsepesh.motorental.model.Booking;
import ge.tsepesh.motorental.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class YooKassaPollingService {

    private final BookingRepository bookingRepository;
    private final YooKassaApiClient yooKassaApiClient;
    private final PaymentConfirmationService paymentConfirmationService;

    /**
     * Опрашивает статусы всех незавершённых платежей.
     */
    @Scheduled(fixedDelayString = "${yookassa.polling.fixed-delay-ms:60000}")
    public void pollPendingPayments() {
        List<Booking> overdue = bookingRepository.findExpiredBookingsByStatus(BookingStatus.PENDING_PAYMENT, LocalDateTime.now());

        if (overdue.isEmpty()) {
            log.debug("No overdue PENDING_PAYMENT bookings to check");
            return;
        }

        log.info("Checking {} overdue PENDING_PAYMENT bookings...", overdue.size());
        for (Booking booking : overdue) {
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
            log.info("Booking {} overdue with no payment ever created — expiring", booking.getId());
            paymentConfirmationService.expireBooking(booking);
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
