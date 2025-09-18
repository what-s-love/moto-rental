package ge.tsepesh.motorental.repository;

import ge.tsepesh.motorental.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {

    @Query("SELECT c FROM Client c WHERE c.email = :email OR c.phone = :phone")
    Optional<Client> findByEmailOrPhone(@Param("email") String email, @Param("phone") String phone);
}
