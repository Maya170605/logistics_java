package com.example.curs4.service;

import com.example.curs4.entity.Role;
import com.example.curs4.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserService userService;

    public boolean isCurrentUser(Long userId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            if (userId == null) {
                return false;
            }

            String currentUsername = authentication.getName();
            if (currentUsername == null) {
                return false;
            }

            User currentUser = userService.findByUsername(currentUsername);
            return currentUser != null && currentUser.getId() != null && currentUser.getId().equals(userId);
        } catch (Exception e) {
            // Логируем ошибку, но возвращаем false вместо выбрасывания исключения
            return false;
        }
    }

    public boolean isCurrentUser(String username, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getName().equals(username);
    }

    public Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            String currentUsername = authentication.getName();
            if (currentUsername == null) {
                return null;
            }

            User currentUser = userService.findByUsername(currentUsername);
            return currentUser != null && currentUser.getId() != null ? currentUser.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Проверяем через authorities
        boolean hasAdminRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));

        if (hasAdminRole) {
            return true;
        }

        // Дополнительная проверка через UserService
        try {
            String currentUsername = authentication.getName();
            User currentUser = userService.findByUsername(currentUsername);
            return currentUser != null && currentUser.getRole() == Role.ADMIN;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return isAdmin(authentication);
    }
}