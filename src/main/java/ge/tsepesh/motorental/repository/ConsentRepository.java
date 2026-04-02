package ge.tsepesh.motorental.repository;

import ge.tsepesh.motorental.model.Consent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConsentRepository extends JpaRepository<Consent, Integer> {

    @Query("SELECT c FROM Consent c WHERE c.client.id = :clientId ORDER BY c.createdAt DESC")
    List<Consent> findByClientIdOrderByCreatedAtDesc(@Param("clientId") Integer clientId);
}
