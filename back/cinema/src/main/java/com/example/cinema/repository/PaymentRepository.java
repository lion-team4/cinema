package com.example.cinema.repository;

import com.example.cinema.entity.Payment;
import com.example.cinema.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findBySubscription(Subscription subscription, Pageable pageable);
    Page<Payment> findBySubscriptionAndPaidAtBetween(
            Subscription subscription,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );
}

