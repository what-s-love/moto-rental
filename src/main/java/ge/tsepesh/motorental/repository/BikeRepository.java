package ge.tsepesh.motorental.repository;

import ge.tsepesh.motorental.model.Bike;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BikeRepository extends JpaRepository<Bike, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Bike b WHERE b.id = :id")
    Optional<Bike> findByIdWithLock(@Param("id") Integer id);

    @Query("SELECT b FROM Bike b WHERE b.limits.id = :limitId AND b.id NOT IN " +
           "(SELECT p.bike.id FROM Participant p WHERE p.ride.date = :date AND p.ride.shift.id = :shiftId)")
    List<Bike> findAvailableBikesForDateAndShift(@Param("limitId") Integer limitId, 
                                                 @Param("date") LocalDate date, 
                                                 @Param("shiftId") Integer shiftId);

    @Query("SELECT b FROM Bike b WHERE b.id NOT IN (SELECT p.bike.id FROM Participant p WHERE p.ride.id = :rideId)")
    List<Bike> findAvailableBikesForRide(@Param("rideId") Integer rideId);
}
