package com.example.curs4.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDTO {

    private Long id;

    @NotNull(message = "User ID обязателен")
    private Long userId;

    private String userName;

    @NotBlank(message = "Описание активности обязательно")
    private String description;

    private LocalDateTime activityDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}