package ge.tsepesh.motorental.repository;

import ge.tsepesh.motorental.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Integer> {

    @Query("SELECT p FROM Policy p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    Optional<Policy> findTopByIsActiveTrueOrderByCreatedAtDesc();

    @Query("SELECT p FROM Policy p ORDER BY p.createdAt DESC")
    List<Policy> findAllOrderByCreatedAtDesc();

    @Transactional
    @Modifying
    @Query("UPDATE Policy p SET p.isActive = false WHERE p.isActive = true")
    void deactivateAllPolicies();
}