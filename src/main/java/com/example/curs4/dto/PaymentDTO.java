package com.example.curs4.dto;

import com.example.curs4.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

    private Long id;

    private String paymentNumber;

    @NotNull(message = "Client ID обязателен")
    private Long clientId;

    private String clientName;

    private Long declarationId;

    private String declarationNumber;

    @NotNull(message = "Сумма обязательна")
    private BigDecimal amount;

    private String currency;

    private String paymentType;

    private PaymentStatus status;

    private LocalDate dueDate;

    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}