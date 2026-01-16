package com.example.cinema.repository.payment;

import com.example.cinema.entity.Payment;
import com.example.cinema.entity.Subscription;
import com.example.cinema.type.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    // 전월 APPROVED 결제 조회 (페이징)
    Page<Payment> findByStatusAndPaidAtBetween(
        PaymentStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );
    
    // 전월 APPROVED 결제 총액 계산 (플랫폼 수입 계산용)
    @Query("SELECT SUM(p.amount) FROM Payment p " +
           "WHERE p.status = :status " +
           "AND p.paidAt BETWEEN :startDate AND :endDate")
    Long sumAmountByStatusAndPaidAtBetween(
        @Param("status") PaymentStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // 전체 기간 APPROVED 결제 총액 계산 (플랫폼 수입 통계용)
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status")
    Long sumTotalAmountByStatus(@Param("status") PaymentStatus status);
}

