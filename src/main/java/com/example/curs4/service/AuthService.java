package com.example.curs4.service;

import com.example.curs4.dto.LoginRequest;
import com.example.curs4.dto.AuthResponse;
import com.example.curs4.entity.User;
import com.example.curs4.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public AuthResponse authenticate(LoginRequest loginRequest) {
        // Аутентификация пользователя
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Генерация JWT токена
        String jwt = jwtTokenProvider.generateToken(authentication);

        // Получение информации о пользователе
        User user = userService.findByUsername(loginRequest.getUsername());

        // Получение ролей
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .roles(roles)
                .name(user.getName())
                .unp(user.getUnp() != null ? user.getUnp().getUnp() : null)
                .activityType(user.getActivityType())
                .verified(user.isVerified())
                .build();
    }
}