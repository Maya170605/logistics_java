package com.example.curs4.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDTO {

    private Long id;

    @NotBlank(message = "Госномер обязателен")
    private String licensePlate;

    private String model;

    private String vehicleType;

    private Integer yearOfManufacture;

    private Double capacity;

    @NotNull(message = "Client ID обязателен")
    private Long clientId;

    private String clientName;

    private Long driverId; // ID водителя, который арендовал машину
    private String driverName; // Имя водителя

    private Boolean isAvailable; // Доступна ли машина для аренды

    private LocalDateTime rentalStartDate; // Дата начала аренды
    private LocalDateTime rentalEndDate; // Дата окончания аренды

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}