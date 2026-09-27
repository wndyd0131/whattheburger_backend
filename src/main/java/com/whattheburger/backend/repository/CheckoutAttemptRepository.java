package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.checkout.CheckoutAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CheckoutAttemptRepository extends JpaRepository<CheckoutAttempt, UUID> {
}
