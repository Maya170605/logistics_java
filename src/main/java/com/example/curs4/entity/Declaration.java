package com.example.curs4.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "declarations")
public class Declaration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String declarationNumber;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    private String declarationType;
    private String tnvedCode;
    private String productDescription;
    private BigDecimal productValue;
    private BigDecimal netWeight;
    private Integer quantity;
    private String countryOfOrigin;
    private String countryOfDestination;
    private String customsOffice;

    // Изменим на String для простоты
    @Builder.Default
    private String status = "PENDING";

    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}