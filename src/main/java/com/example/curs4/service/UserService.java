package com.example.curs4.service;

import com.example.curs4.dto.UserDTO;
import com.example.curs4.entity.Role;
import com.example.curs4.entity.Unp;
import com.example.curs4.entity.User;
import com.example.curs4.exception.CustomException;
import com.example.curs4.mapper.UserMapper;
import com.example.curs4.repository.ActivityRepository;
import com.example.curs4.repository.UnpRepository;
import com.example.curs4.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    @PersistenceContext
    private EntityManager entityManager;
    private final UserRepository userRepository;
    private final UnpRepository unpRepository;
    private final VerificationService verificationService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private ActivityRepository activityRepository;

    // CREATE
    public UserDTO register(UserDTO dto) {
        log.info("Регистрация пользователя: {}", dto.getUsername());

        if (dto.getRole() == Role.ADMIN) {
            throw new CustomException("Роль администратора недоступна для регистрации");
        }

        // Проверяем пароль при создании
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new CustomException("Пароль обязателен");
        }

        validateUser(dto);

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setVerified(true);

        if (dto.getRole() == Role.CLIENT) {
            boolean isVerified = verificationService.verifyUNP(dto.getUnp());
            user.setVerified(isVerified);

            Unp unp = unpRepository.findByUnp(dto.getUnp())
                    .orElseThrow(() -> new CustomException("УНП не найден в справочнике"));
            user.setUnp(unp);
        } else if (dto.getRole() == Role.DRIVER) {
            user.setName(null);
            user.setUnp(null);
            user.setActivityType(null);
        }

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException("Пользователь не аутентифицирован");
        }

        String username = authentication.getName();
        log.info("Получение текущего пользователя: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("Пользователь не найден: " + username));

        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        log.info("Поиск пользователя с ID: {}", id);

        // Используем стандартный метод findById из JpaRepository
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", id);
                    return new CustomException("Пользователь с ID " + id + " не найден");
                });

        log.info("Найден пользователь: {}", user.getUsername());
        UserDTO userDTO = userMapper.toDto(user);
        log.info("DTO создан: {}", userDTO);

        return userDTO;
    }
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    // Метод с пагинацией (если нужен)
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        if (username == null) {
            return null;
        }
        return userRepository.findByUsername(username).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    // UPDATE
    public UserDTO updateUser(Long id, UserDTO dto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new CustomException("Пользователь с ID " + id + " не найден"));

        // Обновляем username, если он изменился
        if (dto.getUsername() != null && !dto.getUsername().equals(existingUser.getUsername())) {
            if (userRepository.existsByUsername(dto.getUsername())) {
                throw new CustomException("Пользователь с таким логином уже существует");
            }
            existingUser.setUsername(dto.getUsername());
        }

        // Обновляем пароль, если он указан
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // Обновляем роль, если указана
        if (dto.getRole() != null) {
            existingUser.setRole(dto.getRole());
        }

        // Обновляем email
        if (dto.getEmail() != null) {
            existingUser.setEmail(dto.getEmail());
        }

        // Обновляем name
        if (dto.getName() != null) {
            existingUser.setName(dto.getName());
        } else if (dto.getRole() == Role.DRIVER) {
            // Для водителя name должен быть null
            existingUser.setName(null);
        }

        // Обновляем activityType
        if (dto.getActivityType() != null) {
            existingUser.setActivityType(dto.getActivityType());
        } else if (dto.getRole() == Role.DRIVER) {
            // Для водителя activityType должен быть null
            existingUser.setActivityType(null);
        }

        // Обновляем УНП для клиентов
        if (dto.getRole() == Role.CLIENT && dto.getUnp() != null && !dto.getUnp().trim().isEmpty()) {
            // Проверяем, что УНП существует в справочнике
            Unp unp = unpRepository.findByUnp(dto.getUnp())
                    .orElseThrow(() -> new CustomException("УНП не найден в справочнике"));
            existingUser.setUnp(unp);
        } else if (dto.getRole() == Role.DRIVER || dto.getRole() == Role.ADMIN) {
            // Для водителя и админа УНП должен быть null
            existingUser.setUnp(null);
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("Пользователь обновлен: {}", updatedUser.getUsername());
        return userMapper.toDto(updatedUser);
    }

    // DELETE
    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException("Пользователь с ID " + id + " не найден"));

        // Проверяем, есть ли связанные декларации или платежи
        // Если есть, можно либо запретить удаление, либо удалить каскадно
        // Для простоты просто удаляем - JPA должен обработать каскадное удаление
        // если настроено в entity, иначе будет ошибка внешнего ключа
        
        try {
            log.info("Начинаем удаление пользователя {} (ID: {})", id);

            // Удаляем в правильном порядке (от дочерних к родительским)

            // 1. Сначала удаляем платежи, которые ссылаются на декларации этого пользователя
            int paymentsFromDeclarations = entityManager.createNativeQuery(
                            "DELETE FROM payments WHERE declaration_id IN (" +
                                    "  SELECT id FROM declarations WHERE client_id = :userId" +
                                    ")")
                    .setParameter("userId", id)
                    .executeUpdate();
            log.info("Удалено платежей по декларациям пользователя: {}", paymentsFromDeclarations);

            // 2. Удаляем платежи, которые напрямую ссылаются на пользователя
            int paymentsDirect = entityManager.createNativeQuery(
                            "DELETE FROM payments WHERE client_id = :userId")
                    .setParameter("userId", id)
                    .executeUpdate();
            log.info("Удалено платежей напрямую связанных с пользователем: {}", paymentsDirect);

            // 3. Удаляем декларации пользователя
            int declarationsDeleted = entityManager.createNativeQuery(
                            "DELETE FROM declarations WHERE client_id = :userId")
                    .setParameter("userId", id)
                    .executeUpdate();
            log.info("Удалено деклараций пользователя: {}", declarationsDeleted);

            int vehiclesDeleted = entityManager.createNativeQuery(
                            "DELETE FROM vehicles WHERE client_id = :userId")
                    .setParameter("userId", id)
                    .executeUpdate();
            log.info("Удалено транспорта пользователя: {}", vehiclesDeleted);

            userRepository.delete(user);
            log.info("Пользователь удален: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Ошибка при удалении пользователя: {}", e.getMessage());
            throw new CustomException("Не удалось удалить пользователя. Возможно, у него есть связанные декларации или платежи.");
        }
    }

    // Валидация
    private void validateUser(UserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new CustomException("Пользователь с таким логином уже существует");
        }

        if (dto.getRole() == Role.CLIENT) {
            String unpValue = dto.getUnp();

            if (unpValue == null || unpValue.trim().isEmpty()) {
                throw new CustomException("УНП обязателен для клиента");
            }

            if (!verificationService.verifyUNP(unpValue)) {
                throw new CustomException("УНП не прошёл валидацию (должно быть 9 цифр)");
            }

            if (unpRepository.findByUnp(unpValue).isEmpty()) {
                throw new CustomException("УНП не найден в справочнике");
            }

            if (userRepository.existsByUnp_Unp(unpValue)) {
                throw new CustomException("Пользователь с таким УНП уже существует");
            }

            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                throw new CustomException("Название компании обязательно для клиента");
            }
        }

        if (dto.getRole() == Role.DRIVER && dto.getName() != null && !dto.getName().trim().isEmpty()) {
            throw new CustomException("Название компании не должно указываться для водителя");
        }
    }

    // Дополнительные методы
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}