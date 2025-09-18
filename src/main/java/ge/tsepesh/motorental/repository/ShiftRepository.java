package ge.tsepesh.motorental.repository;

import ge.tsepesh.motorental.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Integer> {

    @Query("SELECT s FROM Shift s WHERE s.startTime >= :currentTime ORDER BY s.startTime")
    List<Shift> findUpcomingShifts(@Param("currentTime") LocalTime currentTime);
}
