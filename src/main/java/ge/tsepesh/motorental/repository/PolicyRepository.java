package ge.tsepesh.motorental.repository;

import ge.tsepesh.motorental.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Integer> {

    @Query("SELECT p FROM Policy p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    Optional<Policy> findTopByIsActiveTrueOrderByCreatedAtDesc();
}