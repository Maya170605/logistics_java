package com.example.curs4.controller;

import com.example.curs4.dto.ActivityDTO;
import com.example.curs4.exception.CustomException;
import com.example.curs4.service.ActivityService;
import com.example.curs4.service.SecurityService; // ДОБАВЬТЕ ИМПОРТ
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Slf4j
public class ActivityController {

    private final ActivityService activityService;
    private final SecurityService securityService;

    @Operation(summary = "Создать активность")
    @PostMapping
    @PreAuthorize("isAuthenticated()")  // Разрешить ВСЕМ ролям
    public ResponseEntity<ActivityDTO> createActivity(@Valid @RequestBody ActivityDTO dto) {
        log.info("Создание активности для пользователя ID: {}", dto.getUserId());
        ActivityDTO saved = activityService.createActivity(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/user/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActivityDTO> createActivityForUser(
            @PathVariable String username,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        log.info("Создание активности для пользователя: {}, запрос от: {}",
                username, authentication != null ? authentication.getName() : "null");
        log.info("Данные запроса: {}", request);

        String description = request.get("description");
        if (description == null || description.trim().isEmpty()) {
            log.warn("Описание активности пустое");
            return ResponseEntity.badRequest().build();
        }

        try {
            ActivityDTO saved = activityService.createActivityForUser(username, description);
            log.info("Активность успешно создана: ID={}", saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Ошибка создания активности для пользователя {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Создать активность по ID пользователя")
    @PostMapping("/user/id/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActivityDTO> createActivityForUserId(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {

        String description = request.get("description");
        log.info("Создание активности для пользователя ID: {}", userId);

        // Получаем пользователя по ID
        ActivityDTO dto = new ActivityDTO();
        dto.setUserId(userId);
        dto.setDescription(description);
        dto.setActivityDate(LocalDateTime.now());

        ActivityDTO saved = activityService.createActivity(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Получить активность по ID")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActivityDTO> getActivityById(@PathVariable Long id) {
        log.info("Получение активности по ID: {}", id);
        ActivityDTO activity = activityService.getActivityById(id);
        return ResponseEntity.ok(activity);
    }

    @Operation(summary = "Получить все активности")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ActivityDTO>> getAllActivities() {
        log.info("Получение всех активностей");
        List<ActivityDTO> activities = activityService.getAllActivities();
        return ResponseEntity.ok(activities);
    }

    @Operation(summary = "Получить активности по пользователю")
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ActivityDTO>> getActivitiesByUser(@PathVariable Long userId) {
        log.info("Получение активностей для пользователя ID: {}", userId);
        List<ActivityDTO> activities = activityService.getActivitiesByUserId(userId);
        return ResponseEntity.ok(activities);
    }

    @Operation(summary = "Получить последние активности пользователя")
    @GetMapping("/user/{userId}/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ActivityDTO>> getRecentActivitiesByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int limit) {

        try {
            log.info("Получение {} последних активностей пользователя ID: {}", limit, userId);
            List<ActivityDTO> activities = activityService.getRecentActivitiesByUserId(userId, limit);
            return ResponseEntity.ok(activities != null ? activities : List.of());
        } catch (Exception e) {
            log.error("Ошибка при получении активностей для пользователя ID: {}", userId, e);
            return ResponseEntity.ok(List.of()); // Возвращаем пустой список вместо ошибки
        }
    }

    @Operation(summary = "Получить активности пользователя с пагинацией")
    @GetMapping("/user/{userId}/page")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ActivityDTO>> getActivitiesByUserWithPagination(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Получение активностей пользователя ID: {} - страница: {}, размер: {}", userId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityDTO> activities = activityService.getActivitiesByUserId(userId, pageable);
        return ResponseEntity.ok(activities);
    }

    @Operation(summary = "Обновить активность")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActivityDTO> updateActivity(@PathVariable Long id,
                                                      @Valid @RequestBody ActivityDTO dto) {
        log.info("Обновление активности ID: {}", id);
        ActivityDTO updated = activityService.updateActivity(id, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить активность")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        log.info("Удаление активности ID: {}", id);
        activityService.deleteActivity(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить все активности пользователя")
    @DeleteMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAllActivitiesByUser(@PathVariable Long userId) {
        log.info("Удаление всех активностей пользователя ID: {}", userId);
        activityService.deleteAllActivitiesByUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить статистику по пользователю")
    @GetMapping("/user/{userId}/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable Long userId) {
        log.info("Получение статистики активностей для пользователя ID: {}", userId);

        long totalActivities = activityService.getActivitiesCountByUser(userId);
        long todayActivities = activityService.getTodayActivitiesCountByUser(userId);

        return ResponseEntity.ok(Map.of(
                "totalActivities", totalActivities,
                "todayActivities", todayActivities
        ));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<String> handleCustomException(CustomException ex) {
        log.error("Ошибка: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        log.error("Внутренняя ошибка сервера", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Произошла внутренняя ошибка сервера");
    }
}