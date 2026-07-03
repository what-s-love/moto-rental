package ge.tsepesh.motorental.repository;

import ge.tsepesh.motorental.enums.Difficulty;
import ge.tsepesh.motorental.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Integer> {

    @Query("SELECT r FROM Route r WHERE r.enabled = true AND r.isSpecial = false")
    List<Route> findActiveNonSpecialRoutes();

    @Query("SELECT r FROM Route r WHERE r.enabled = true")
    List<Route> findActiveRoutes();

    @Query("SELECT r FROM Route r WHERE r.difficulty IN :difficulties ORDER BY r.difficulty, r.price")
    List<Route> findByDifficultiesOrderByDifficultyAndPrice(@Param("difficulties") List<Difficulty> difficulties);

    @Query("SELECT r FROM Route r WHERE r.name = :name")
    Optional<Route> findByName(@Param("name") String name);
}
