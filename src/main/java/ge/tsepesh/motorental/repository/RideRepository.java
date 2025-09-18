package ge.tsepesh.motorental.repository;

import ge.tsepesh.motorental.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RideRepository extends JpaRepository<Ride, Integer> {

    @Query("SELECT r FROM Ride r WHERE r.date = :date AND r.shift.id = :shiftId")
    Optional<Ride> findByDateAndShift(@Param("date") LocalDate date, @Param("shiftId") Integer shiftId);

    @Query("SELECT r FROM Ride r WHERE r.date BETWEEN :startDate AND :endDate ORDER BY r.date, r.shift.startTime")
    List<Ride> findRidesByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
