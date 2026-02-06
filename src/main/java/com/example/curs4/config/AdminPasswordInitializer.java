package com.example.curs4.config;

import com.example.curs4.entity.Role;
import com.example.curs4.entity.User;
import com.example.curs4.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Инициализатор для создания/обновления администратора при запуске приложения
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminPasswordInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Optional<User> adminOpt = userRepository.findByUsername("admin");
        if (adminOpt.isPresent()) {
            User admin = adminOpt.get();
            // Обновляем пароль администратора
            String correctHash = passwordEncoder.encode("admin123");
            admin.setPassword(correctHash);
            admin.setRole(Role.ADMIN);
            admin.setVerified(true);
            userRepository.save(admin);
            log.info("Пароль администратора обновлен. Username: admin, Password: admin123");
        } else {
            // Создаем нового администратора
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .verified(true)
                    .name("Администратор")
                    .email("admin@example.com")
                    .build();
            userRepository.save(admin);
            log.info("Администратор создан. Username: admin, Password: admin123");
        }
    }
}

