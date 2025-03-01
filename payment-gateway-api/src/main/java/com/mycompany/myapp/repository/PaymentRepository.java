package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Payment;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.nio.channels.FileChannel;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Payment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByExternalId(String externalId);
}
