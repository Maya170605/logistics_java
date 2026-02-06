package com.example.curs4.controller;

import com.example.curs4.dto.VehicleDTO;
import com.example.curs4.exception.CustomException;
import com.example.curs4.service.VehicleService;
import com.example.curs4.service.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Slf4j
public class VehicleController {

    private final VehicleService vehicleService;
    private final SecurityService securityService;

    @Operation(summary = "Создать транспорт")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<VehicleDTO> createVehicle(@Valid @RequestBody VehicleDTO dto) {
        log.info("Создание транспорта для клиента ID: {}", dto.getClientId());
        VehicleDTO saved = vehicleService.createVehicle(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Получить транспорт по ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'DRIVER')")
    public ResponseEntity<VehicleDTO> getVehicleById(@PathVariable Long id) {
        log.info("Получение транспорта по ID: {}", id);
        VehicleDTO vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(vehicle);
    }

    @Operation(summary = "Получить все транспортные средства")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'DRIVER')")
    public ResponseEntity<List<VehicleDTO>> getAllVehicles() {
        log.info("Получение всех транспортных средств");
        List<VehicleDTO> vehicles = vehicleService.getAllVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @Operation(summary = "Получить доступные для аренды машины")
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<List<VehicleDTO>> getAvailableVehicles() {
        log.info("Получение доступных для аренды машин");
        List<VehicleDTO> vehicles = vehicleService.getAvailableVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @Operation(summary = "Получить машины, арендованные водителем")
    @GetMapping("/driver/{driverId}/rented")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER') and (@securityService.isCurrentUser(#driverId, authentication) or hasRole('ADMIN'))")
    public ResponseEntity<List<VehicleDTO>> getRentedVehiclesByDriver(@PathVariable Long driverId) {
        log.info("Получение машин, арендованных водителем ID: {}", driverId);
        List<VehicleDTO> vehicles = vehicleService.getRentedVehiclesByDriver(driverId);
        return ResponseEntity.ok(vehicles);
    }

    @Operation(summary = "Получить все арендованные машины")
    @GetMapping("/rented")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VehicleDTO>> getAllRentedVehicles() {
        log.info("Получение всех арендованных машин");
        List<VehicleDTO> vehicles = vehicleService.getAllRentedVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @Operation(summary = "Арендовать машину")
    @PostMapping("/{vehicleId}/rent")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<VehicleDTO> rentVehicle(
            @PathVariable Long vehicleId,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            log.info("Аренда машины ID: {}", vehicleId);
            
            // Получаем ID текущего водителя из SecurityContext
            Long driverId = securityService.getCurrentUserId();
            if (driverId == null) {
                throw new CustomException("Не удалось определить текущего пользователя");
            }
            
            LocalDateTime endDate = null;
            if (request != null && request.containsKey("days")) {
                // Если передан параметр days (количество дней)
                Integer days = Integer.valueOf(request.get("days").toString());
                if (days <= 0) {
                    throw new CustomException("Количество дней аренды должно быть положительным");
                }
                endDate = LocalDateTime.now().plusDays(days);
            } else if (request != null && request.containsKey("endDate")) {
                // Если передан endDate напрямую
                try {
                    String endDateStr = request.get("endDate").toString();
                    // Пробуем разные форматы
                    if (endDateStr.contains("T")) {
                        endDate = LocalDateTime.parse(endDateStr);
                    } else {
                        // Если только дата, добавляем время
                        endDate = LocalDateTime.parse(endDateStr + "T00:00:00");
                    }
                } catch (Exception e) {
                    log.error("Ошибка парсинга даты: {}", e.getMessage());
                    throw new CustomException("Неверный формат даты окончания аренды");
                }
            } else {
                // По умолчанию аренда на 30 дней
                endDate = LocalDateTime.now().plusDays(30);
            }
            
            VehicleDTO rented = vehicleService.rentVehicle(vehicleId, driverId, endDate);
            return ResponseEntity.ok(rented);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка при аренде машины: {}", e.getMessage(), e);
            throw new CustomException("Ошибка при аренде машины: " + e.getMessage());
        }
    }

    @Operation(summary = "Вернуть машину")
    @PostMapping("/{vehicleId}/return")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<VehicleDTO> returnVehicle(@PathVariable Long vehicleId) {
        log.info("Возврат машины ID: {}", vehicleId);
        
        // Получаем ID текущего водителя из SecurityContext
        Long driverId = securityService.getCurrentUserId();
        if (driverId == null) {
            throw new CustomException("Не удалось определить текущего пользователя");
        }
        
        VehicleDTO returned = vehicleService.returnVehicle(vehicleId, driverId);
        return ResponseEntity.ok(returned);
    }

    @Operation(summary = "Получить транспорт по клиенту")
    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and @securityService.isCurrentUser(#clientId, authentication))")
    public ResponseEntity<List<VehicleDTO>> getVehiclesByClient(@PathVariable Long clientId) {
        log.info("Получение транспорта для клиента ID: {}", clientId);
        List<VehicleDTO> vehicles = vehicleService.getVehiclesByClientId(clientId);
        return ResponseEntity.ok(vehicles);
    }

    @Operation(summary = "Получить транспорт по типу")
    @GetMapping("/type/{vehicleType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VehicleDTO>> getVehiclesByType(@PathVariable String vehicleType) {
        log.info("Получение транспорта по типу: {}", vehicleType);
        List<VehicleDTO> vehicles = vehicleService.getVehiclesByType(vehicleType);
        return ResponseEntity.ok(vehicles);
    }

    @Operation(summary = "Получить транспорт по госномеру")
    @GetMapping("/license-plate/{licensePlate}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and @vehicleService.isVehicleLicensePlateOwner(#licensePlate, authentication))")
    public ResponseEntity<VehicleDTO> getVehicleByLicensePlate(@PathVariable String licensePlate) {
        log.info("Получение транспорта по госномеру: {}", licensePlate);
        VehicleDTO vehicle = vehicleService.getVehicleByLicensePlate(licensePlate);
        return ResponseEntity.ok(vehicle);
    }

    @Operation(summary = "Обновить транспорт")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN') and (hasRole('ADMIN') or @vehicleService.isSingleVehicleOwner(#id, authentication))")
    public ResponseEntity<VehicleDTO> updateVehicle(@PathVariable Long id,
                                                    @Valid @RequestBody VehicleDTO dto) {
        log.info("Обновление транспорта ID: {}", id);
        VehicleDTO updated = vehicleService.updateVehicle(id, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить транспорт")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN') and (hasRole('ADMIN') or @vehicleService.isSingleVehicleOwner(#id, authentication))")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        log.info("Удаление транспорта ID: {}", id);
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Проверить существование госномера")
    @GetMapping("/check-license-plate/{licensePlate}")
    public ResponseEntity<Map<String, Boolean>> checkLicensePlateExists(@PathVariable String licensePlate) {
        log.info("Проверка госномера: {}", licensePlate);
        boolean exists = vehicleService.licensePlateExists(licensePlate);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @Operation(summary = "Получить статистику по клиенту")
    @GetMapping("/client/{clientId}/stats")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and @securityService.isCurrentUser(#clientId, authentication))")
    public ResponseEntity<Map<String, Object>> getClientStats(@PathVariable Long clientId) {
        log.info("Получение статистики транспорта для клиента ID: {}", clientId);

        long totalVehicles = vehicleService.getVehiclesCountByClient(clientId);
        long trucksCount = vehicleService.getTrucksCountByClient(clientId);
        double totalCapacity = vehicleService.getTotalCapacityByClient(clientId);

        return ResponseEntity.ok(Map.of(
                "totalVehicles", totalVehicles,
                "trucksCount", trucksCount,
                "totalCapacity", totalCapacity
        ));
    }

    // Exception Handlers
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