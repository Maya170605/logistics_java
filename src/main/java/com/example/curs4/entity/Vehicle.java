package com.example.curs4.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String licensePlate;

    private String model;
    private String vehicleType;
    private Integer yearOfManufacture;
    private Double capacity;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver; // Водитель, который арендовал машину

    @Builder.Default
    @Column(name = "is_available")
    private Boolean isAvailable = true; // Доступна ли машина для аренды

    @Column(name = "rental_start_date")
    private LocalDateTime rentalStartDate; // Дата начала аренды

    @Column(name = "rental_end_date")
    private LocalDateTime rentalEndDate; // Дата окончания аренды

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}