package com.example.curs4.service;

import com.example.curs4.dto.ActivityDTO;
import com.example.curs4.entity.Activity;
import com.example.curs4.entity.User;
import com.example.curs4.exception.CustomException;
import com.example.curs4.mapper.ActivityMapper;
import com.example.curs4.repository.ActivityRepository;
import com.example.curs4.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final ActivityMapper activityMapper;

    // CREATE
    public ActivityDTO createActivity(ActivityDTO dto) {
        log.info("Создание активности для пользователя ID: {}", dto.getUserId());

        validateActivity(dto);

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new CustomException("Пользователь не найден"));

        Activity activity = activityMapper.toEntity(dto);
        activity.setUser(user);

        // Если дата активности не указана, устанавливаем текущую
        if (activity.getActivityDate() == null) {
            activity.setActivityDate(LocalDateTime.now());
        }

        Activity savedActivity = activityRepository.save(activity);
        log.info("Активность создана: {}", savedActivity.getDescription());

        return activityMapper.toDto(savedActivity);
    }

    // CREATE ACTIVITY WITH USERNAME (удобный метод)
    public ActivityDTO createActivityForUser(String username, String description) {
        log.info("Создание активности для пользователя: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("Пользователь не найден"));

        Activity activity = Activity.builder()
                .user(user)
                .description(description)
                .activityDate(LocalDateTime.now())
                .build();

        Activity savedActivity = activityRepository.save(activity);
        log.info("Активность создана: {}", description);

        return activityMapper.toDto(savedActivity);
    }

    // READ
    @Transactional(readOnly = true)
    public ActivityDTO getActivityById(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new CustomException("Активность не найдена"));
        return activityMapper.toDto(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityDTO> getAllActivities() {
        return activityRepository.findAll().stream()
                .map(activityMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActivityDTO> getActivitiesByUserId(Long userId) {
        return activityRepository.findByUserIdOrderByActivityDateDesc(userId).stream()
                .map(activityMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActivityDTO> getRecentActivitiesByUserId(Long userId, int limit) {
        try {
            if (userId == null) {
                log.warn("Попытка получить активности для null userId");
                return List.of();
            }

            if (limit <= 0) {
                limit = 5; // default
            }

            List<Activity> activities;
            if (limit == 5) {
                activities = activityRepository.findTop5ByUserIdOrderByActivityDateDesc(userId);
            } else {
                // Для других лимитов используем пагинацию
                Page<Activity> page = activityRepository.findByUserIdOrderByActivityDateDesc(
                        userId, Pageable.ofSize(limit));
                activities = page != null ? page.getContent() : List.of();
            }

            if (activities == null) {
                return List.of();
            }

            return activities.stream()
                    .map(activity -> {
                        try {
                            return activityMapper.toDto(activity);
                        } catch (Exception e) {
                            log.error("Ошибка при маппинге активности ID: {}", activity != null ? activity.getId() : "null", e);
                            return null;
                        }
                    })
                    .filter(dto -> dto != null) // Фильтруем null значения
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Ошибка при получении активностей для пользователя ID: {}", userId, e);
            return List.of(); // Возвращаем пустой список вместо выбрасывания исключения
        }
    }

    @Transactional(readOnly = true)
    public Page<ActivityDTO> getActivitiesByUserId(Long userId, Pageable pageable) {
        return activityRepository.findByUserIdOrderByActivityDateDesc(userId, pageable)
                .map(activityMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<ActivityDTO> getActivitiesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return activityRepository.findByActivityDateBetweenOrderByActivityDateDesc(startDate, endDate).stream()
                .map(activityMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActivityDTO> getActivitiesByUserAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Пользователь не найден"));

        return activityRepository.findByUserAndActivityDateBetweenOrderByActivityDateDesc(user, startDate, endDate).stream()
                .map(activityMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActivityDTO> searchActivitiesByUserAndKeyword(Long userId, String keyword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Пользователь не найден"));

        return activityRepository.findByUserAndDescriptionContainingIgnoreCase(user, keyword).stream()
                .map(activityMapper::toDto)
                .collect(Collectors.toList());
    }

    // UPDATE
    public ActivityDTO updateActivity(Long id, ActivityDTO dto) {
        Activity existingActivity = activityRepository.findById(id)
                .orElseThrow(() -> new CustomException("Активность не найдена"));

        // Обновляем поля
        existingActivity.setDescription(dto.getDescription());
        if (dto.getActivityDate() != null) {
            existingActivity.setActivityDate(dto.getActivityDate());
        }

        Activity updatedActivity = activityRepository.save(existingActivity);
        log.info("Активность обновлена: {}", updatedActivity.getDescription());

        return activityMapper.toDto(updatedActivity);
    }

    // DELETE
    public void deleteActivity(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new CustomException("Активность не найдена"));

        activityRepository.delete(activity);
        log.info("Активность удалена: {}", activity.getDescription());
    }

    // DELETE ALL BY USER
    public void deleteAllActivitiesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Пользователь не найден"));

        List<Activity> activities = activityRepository.findByUserOrderByActivityDateDesc(user);
        activityRepository.deleteAll(activities);

        log.info("Удалено {} активностей пользователя ID: {}", activities.size(), userId);
    }

    // VALIDATION
    private void validateActivity(ActivityDTO dto) {
        if (dto.getUserId() == null) {
            throw new CustomException("ID пользователя обязателен");
        }

        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new CustomException("Описание активности обязательно");
        }
    }

    // STATISTICS
    @Transactional(readOnly = true)
    public long getActivitiesCountByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Пользователь не найден"));
        return activityRepository.countByUser(user);
    }

    @Transactional(readOnly = true)
    public long getTodayActivitiesCountByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Пользователь не найден"));

        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<Activity> activities = activityRepository.findByUserAndActivityDateBetweenOrderByActivityDateDesc(
                user, startOfDay, endOfDay);

        return activities.size();
    }

    // UTILITY METHODS
    @Transactional(readOnly = true)
    public boolean activityExists(Long id) {
        return activityRepository.existsById(id);
    }
    public boolean isUserOwner(Long userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new CustomException("Пользователь не найден"));

        return currentUser.getId().equals(userId);
    }

    public boolean isActivityOwner(Long activityId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new CustomException("Активность не найдена"));

        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new CustomException("Пользователь не найден"));

        return activity.getUser().getId().equals(currentUser.getId());
    }
}