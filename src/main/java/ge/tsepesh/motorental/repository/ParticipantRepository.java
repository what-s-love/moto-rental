package ge.tsepesh.motorental.repository;

import ge.tsepesh.motorental.enums.ExperienceLevel;
import ge.tsepesh.motorental.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Integer> {

    @Query("SELECT COUNT(p) FROM Participant p WHERE p.ride.date = :date AND p.ride.shift.id = :shiftId AND p.experienceLevel = :experienceLevel")
    Long countByDateShiftAndExperience(@Param("date") LocalDate date, 
                                      @Param("shiftId") Integer shiftId, 
                                      @Param("experienceLevel") ExperienceLevel experienceLevel);

    @Query("SELECT p FROM Participant p WHERE p.ride.id = :rideId ORDER BY p.client.name")
    List<Participant> findByRideIdOrderByClientName(@Param("rideId") Integer rideId);
}
