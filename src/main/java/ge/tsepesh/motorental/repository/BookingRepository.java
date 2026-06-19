package ge.tsepesh.motorental.repository;

import ge.tsepesh.motorental.enums.BookingStatus;
import ge.tsepesh.motorental.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = :status AND b.expiresAt < :currentTime")
    List<Booking> findExpiredBookingsByStatus(@Param("status") BookingStatus status, 
                                             @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b WHERE b.client.id = :clientId ORDER BY b.createdAt DESC")
    List<Booking> findByClientIdOrderByCreatedAtDesc(@Param("clientId") Integer clientId);

    @Override
    Optional<Booking> findById(Integer integer);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.client " +
            "JOIN FETCH b.ride r " +
            "JOIN FETCH r.shift " +
            "JOIN FETCH r.route " +
            "WHERE YEAR(r.date) = :year AND MONTH(r.date) = :month " +
            "ORDER BY r.date, r.shift.startTime")
    List<Booking> findBookingsByYearAndMonth(@Param("year") int year, @Param("month") int month);

    /**
     * Все бронирования в указанном статусе, у которых уже привязан платёж
     * (используется polling-сервисом для опроса незавершённых платежей).
     */
    @Query("SELECT b FROM Booking b JOIN FETCH b.payment p " +
            "WHERE b.bookingStatus = :status AND p.transactionRef IS NOT NULL")
    List<Booking> findByStatusWithPayment(@Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b ORDER BY b.createdAt DESC")
    Page<Booking> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
