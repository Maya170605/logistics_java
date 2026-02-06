package com.example.curs4.dto;

import com.example.curs4.entity.Role;
import jakarta.validation.constraints.*;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;

    private String name; // Теперь не обязательно для всех

    private String unp; // теперь не обязательно для всех

    @Email(message = "Неверный тип email")
    private String email;

    private String activityType; // теперь не обязательно для всех

    // Новые поля
    @NotBlank(message = "Логин обязателен")
    private String username;

    // Пароль опционален при обновлении (может быть null или пустым)
    private String password;

    @NotNull(message = "Роль обязательна")
    private Role role;
}