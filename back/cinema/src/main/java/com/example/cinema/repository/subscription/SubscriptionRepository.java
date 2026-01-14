package com.example.cinema.repository.subscription;

import com.example.cinema.entity.Subscription;
import com.example.cinema.entity.User;
import com.example.cinema.type.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findBySubscriber(User subscriber);
    boolean existsBySubscriber(User subscriber);

    List<Subscription> findByStatusAndCurrentPeriodEndBefore(SubscriptionStatus status, LocalDateTime endDateTime);
}

