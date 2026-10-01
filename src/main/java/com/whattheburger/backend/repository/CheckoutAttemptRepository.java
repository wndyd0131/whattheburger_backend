package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.checkout.CheckoutAttempt;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CheckoutAttemptRepository extends JpaRepository<CheckoutAttempt, UUID> {
    Optional<CheckoutAttempt> findByCheckoutSessionId(String checkoutSessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ca FROM CheckoutAttempt ca WHERE ca.checkoutAttemptId = :id")
    Optional<CheckoutAttempt> findByIdForUpdate(@Param("id") UUID id);
}
