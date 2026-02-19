package com.example.curs4.controller;

import com.example.curs4.dto.UserDTO;
import com.example.curs4.entity.User;
import com.example.curs4.exception.CustomException;
import com.example.curs4.repository.UserRepository;
import com.example.curs4.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @Operation(summary = "Создать пользователя (только админ)")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'DRIVER')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO dto) {
        log.info("Создание пользователя: {}", dto.getUsername());
        UserDTO saved = userService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Получить пользователя по ID (только админ)")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'DRIVER')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        log.info("Получение пользователя по ID: {}", id);
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Получить всех пользователей (только админ)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("Получение всех пользователей");
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Получить пользователей по роли (только админ)")
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable String role) {
        log.info("Получение пользователей с ролью: {}", role);
        List<UserDTO> users = userService.getUsersByRole(com.example.curs4.entity.Role.valueOf(role.toUpperCase()));
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Получить текущего пользователя")
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'DRIVER')")
    public ResponseEntity<UserDTO> getCurrentUser() {
        log.info("Получение текущего пользователя");
        UserDTO user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Обновить пользователя")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'DRIVER')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO dto) {
        try {
            log.info("Обновление пользователя ID: {}", id);
            // Если username или role не указаны, получаем их из существующего пользователя
            if (dto.getUsername() == null || dto.getRole() == null) {
                User existingUser = userRepository.findById(id)
                        .orElseThrow(() -> new CustomException("Пользователь не найден"));
                if (dto.getUsername() == null) {
                    dto.setUsername(existingUser.getUsername());
                }
                if (dto.getRole() == null) {
                    dto.setRole(existingUser.getRole());
                }
            }
            UserDTO updated = userService.updateUser(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Ошибка при обновлении пользователя ID: {}", id, e);
            throw e;
        }
    }

    @Operation(summary = "Удалить пользователя")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'DRIVER')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Удаление пользователя ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Проверить существование username (публичный)")
    @GetMapping("/check-username/{username}")
    public ResponseEntity<Map<String, Boolean>> checkUsernameExists(@PathVariable String username) {
        log.info("Проверка username: {}", username);
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @Operation(summary = "Проверить существование email (публичный)")
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(@PathVariable String email) {
        log.info("Проверка email: {}", email);
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }



    // Обработчики исключений
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<String> handleCustomException(CustomException ex) {
        log.error("Ошибка: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .toList();
        log.warn("Ошибки валидации: {}", errors);
        return ResponseEntity.badRequest().body(Map.of("errors", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        log.error("Внутренняя ошибка сервера", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Произошла внутренняя ошибка сервера");
    }
}