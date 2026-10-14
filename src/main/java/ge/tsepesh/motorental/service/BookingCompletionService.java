package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.enums.BookingStatus;
import ge.tsepesh.motorental.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Переводит оплаченные бронирования в статус COMPLETED после окончания заезда.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingCompletionService {
    private final BookingRepository bookingRepository;

    @Scheduled(cron = "${booking.completion.cron:0 */30 * * * *}")
    @Transactional
    public void completeFinishedRides() {
        LocalDateTime now = LocalDateTime.now();
        int updated = bookingRepository.updateStatusForFinishedRides(
                BookingStatus.PAID, BookingStatus.COMPLETED,
                now.toLocalDate(), now.toLocalTime());
        if (updated > 0) {
            log.info("{} PAID bookings marked as COMPLETED (rides finished before {})", updated, now);
        }
    }
}