package com.example.curs4.controller;

import com.example.curs4.dto.UserDTO;
import com.example.curs4.dto.LoginRequest;
import com.example.curs4.dto.AuthResponse;
import com.example.curs4.service.UserService;
import com.example.curs4.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO dto) {
        log.info("Регистрация пользователя: {}", dto.getUsername());

        try {
            UserDTO registeredUser = userService.register(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Авторизация пользователя")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Авторизация пользователя: {}", loginRequest.getUsername());

        try {
            AuthResponse authResponse = authService.authenticate(loginRequest);
            log.info("Успешная авторизация пользователя: {}", loginRequest.getUsername());
            return ResponseEntity.ok(authResponse);
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            log.warn("Неверные учетные данные для пользователя: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Неверные учетные данные"));
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            log.warn("Пользователь не найден: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Пользователь не найден"));
        } catch (Exception e) {
            log.error("Ошибка при авторизации пользователя {}: {}", loginRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Неверные учетные данные"));
        }
    }

    @Operation(summary = "Проверить доступность username")
    @GetMapping("/check-username/{username}")
    public ResponseEntity<Map<String, Boolean>> checkUsernameExists(@PathVariable String username) {
        log.info("Проверка username: {}", username);
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @Operation(summary = "Проверить доступность email")
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(@PathVariable String email) {
        log.info("Проверка email: {}", email);
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}