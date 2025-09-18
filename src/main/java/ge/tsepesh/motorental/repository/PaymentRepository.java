package ge.tsepesh.motorental.repository;

import ge.tsepesh.motorental.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    @Query("SELECT p FROM Payment p WHERE p.transactionRef = :transactionRef")
    Optional<Payment> findByTransactionRef(@Param("transactionRef") String transactionRef);
}
