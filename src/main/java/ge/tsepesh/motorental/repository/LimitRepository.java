package ge.tsepesh.motorental.repository;

import ge.tsepesh.motorental.model.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LimitRepository extends JpaRepository<Limit, Integer> {

    @Query("SELECT l FROM Limit l WHERE l.heightMin <= :height AND l.heightMax >= :height AND l.ageMin <= :age AND (:isMale = true OR l.onlyMen = false)")
    List<Limit> findSuitableLimitsForParticipant(@Param("height") Integer height, @Param("age") Integer age, @Param("isMale") Boolean isMale);
}
