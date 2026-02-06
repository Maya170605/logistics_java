package com.example.curs4.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeclarationDTO {

    private Long id;

    private String declarationNumber;

    @NotNull(message = "Client ID обязателен")
    private Long clientId;

    private String clientName; // Для отображения

    @NotBlank(message = "Тип декларации обязателен")
    private String declarationType;

    private String tnvedCode;

    @NotBlank(message = "Описание товара обязательно")
    private String productDescription;

    @NotNull(message = "Стоимость товара обязательна")
    private BigDecimal productValue;

    private BigDecimal netWeight;

    private Integer quantity;

    private String countryOfOrigin;

    private String countryOfDestination;

    private String customsOffice;

    private String status;

    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}