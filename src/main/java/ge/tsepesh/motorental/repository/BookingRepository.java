package ge.tsepesh.motorental.repository;

import ge.tsepesh.motorental.enums.BookingStatus;
import ge.tsepesh.motorental.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = :status AND b.expiresAt < :currentTime")
    List<Booking> findExpiredBookingsByStatus(@Param("status") BookingStatus status, 
                                             @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b WHERE b.client.id = :clientId ORDER BY b.createdAt DESC")
    List<Booking> findByClientIdOrderByCreatedAtDesc(@Param("clientId") Integer clientId);
}
