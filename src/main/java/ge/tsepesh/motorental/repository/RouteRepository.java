package ge.tsepesh.motorental.repository;

import ge.tsepesh.motorental.enums.Difficulty;
import ge.tsepesh.motorental.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Integer> {

    @Query("SELECT r FROM Route r WHERE r.difficulty IN :difficulties ORDER BY r.difficulty, r.price")
    List<Route> findByDifficultiesOrderByDifficultyAndPrice(@Param("difficulties") List<Difficulty> difficulties);
}
