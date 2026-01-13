package com.example.cinema.repository;

import com.example.cinema.entity.Subscription;
import com.example.cinema.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findBySubscriber(User subscriber);
    boolean existsBySubscriber(User subscriber);
}

