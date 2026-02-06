package com.example.curs4.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_number", unique = true, nullable = false)
    private String paymentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "declaration_id")
    private Declaration declaration;

    @Column(nullable = false)
    private BigDecimal amount;

    @Builder.Default
    private String currency = "EUR";

    @Column(name = "payment_type")
    private String paymentType; // пошлина, НДС, акциз

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private com.example.curs4.entity.PaymentStatus status = com.example.curs4.entity.PaymentStatus.PENDING;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}