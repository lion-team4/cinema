package com.example.cinema.repository.billing;

import com.example.cinema.entity.BillingKey;
import com.example.cinema.entity.User;
import com.example.cinema.type.BillingKeyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillingKeyRepository extends JpaRepository<BillingKey, Long> {
    Optional<BillingKey> findByUserAndStatus(User user, BillingKeyStatus status);
    boolean existsByUserAndStatus(User user, BillingKeyStatus status);
}

