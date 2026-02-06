package com.example.curs4.repository;

import com.example.curs4.entity.Payment;
import com.example.curs4.entity.PaymentStatus;
import com.example.curs4.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByClient(User client);

    List<Payment> findByClientId(Long clientId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByDeclarationId(Long declarationId);

    Optional<Payment> findByPaymentNumber(String paymentNumber);

    boolean existsByPaymentNumber(String paymentNumber);

    long countByClient(User client);

    long countByClientAndStatus(User client, PaymentStatus status);

    // Статистические методы
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.client = :client")
    BigDecimal getTotalAmountByClient(@Param("client") User client);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.client = :client AND p.status = 'PAID'")
    BigDecimal getTotalPaidAmountByClient(@Param("client") User client);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.client = :client AND p.status = 'PENDING'")
    BigDecimal getTotalPendingAmountByClient(@Param("client") User client);

    @Query("SELECT p FROM Payment p WHERE p.client = :client AND p.dueDate < :today AND p.status = 'PENDING'")
    List<Payment> findOverduePayments(@Param("client") User client, @Param("today") LocalDate today);
}