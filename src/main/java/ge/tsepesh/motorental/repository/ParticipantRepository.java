package ge.tsepesh.motorental.repository;

import ge.tsepesh.motorental.enums.BookingStatus;
import ge.tsepesh.motorental.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Integer> {
    @Query("""
            SELECT COUNT(p) FROM Participant p
            WHERE p.ride.date = :date
              AND p.ride.shift.id = :shiftId
              AND p.booking.bookingStatus IN :activeStatuses
    """)
    Long countByDateAndShift(@Param("date") LocalDate date,
                             @Param("shiftId") Integer shiftId,
                             @Param("activeStatuses") List<BookingStatus> activeStatuses);

    @Query("""
            SELECT p.bike.id FROM Participant p
            WHERE p.ride.date = :date
              AND p.ride.shift.id = :shiftId
              AND p.booking.bookingStatus IN :activeStatuses
    """)
    List<Integer> findOccupiedBikeIds(@Param("date") LocalDate date,
                                      @Param("shiftId") Integer shiftId,
                                      @Param("activeStatuses") List<BookingStatus> activeStatuses);

    @Query("SELECT p FROM Participant p WHERE p.booking.id = :bookingId ORDER BY p.client.name")
    List<Participant> findByBookingIdOrderByClientName(@Param("bookingId") Integer bookingId);
}