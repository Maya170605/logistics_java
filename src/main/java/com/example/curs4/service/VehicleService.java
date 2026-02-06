package com.example.curs4.service;

import com.example.curs4.dto.VehicleDTO;
import com.example.curs4.entity.User;
import com.example.curs4.entity.Vehicle;
import com.example.curs4.exception.CustomException;
import com.example.curs4.mapper.VehicleMapper;
import com.example.curs4.repository.UserRepository;
import com.example.curs4.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final VehicleMapper vehicleMapper;

    // CREATE
    public VehicleDTO createVehicle(VehicleDTO dto) {
        log.info("Создание транспорта для клиента ID: {}", dto.getClientId());

        validateVehicle(dto);

        // Проверяем уникальность госномера
        if (vehicleRepository.existsByLicensePlate(dto.getLicensePlate())) {
            throw new CustomException("Транспорт с номером " + dto.getLicensePlate() + " уже существует");
        }

        User client = userRepository.findById(dto.getClientId())
                .orElseThrow(() -> new CustomException("Клиент не найден"));

        Vehicle vehicle = vehicleMapper.toEntity(dto);
        vehicle.setClient(client);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Транспорт создан: {}", savedVehicle.getLicensePlate());

        return vehicleMapper.toDto(savedVehicle);
    }

    // READ
    @Transactional(readOnly = true)
    public VehicleDTO getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new CustomException("Транспорт не найден"));
        return vehicleMapper.toDto(vehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleDTO> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(vehicleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VehicleDTO> getVehiclesByClientId(Long clientId) {
        return vehicleRepository.findByClientId(clientId).stream()
                .map(vehicleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VehicleDTO> getVehiclesByType(String vehicleType) {
        return vehicleRepository.findByVehicleTypeContainingIgnoreCase(vehicleType).stream()
                .map(vehicleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VehicleDTO getVehicleByLicensePlate(String licensePlate) {
        Vehicle vehicle = vehicleRepository.findByLicensePlate(licensePlate)
                .orElseThrow(() -> new CustomException("Транспорт с номером " + licensePlate + " не найден"));
        return vehicleMapper.toDto(vehicle);
    }

    // UPDATE
    public VehicleDTO updateVehicle(Long id, VehicleDTO dto) {
        Vehicle existingVehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new CustomException("Транспорт не найден"));

        // Проверяем уникальность госномера (если изменился)
        if (!existingVehicle.getLicensePlate().equals(dto.getLicensePlate()) &&
                vehicleRepository.existsByLicensePlate(dto.getLicensePlate())) {
            throw new CustomException("Транспорт с номером " + dto.getLicensePlate() + " уже существует");
        }

        // Обновляем владельца, если указан новый clientId
        if (dto.getClientId() != null && !dto.getClientId().equals(existingVehicle.getClient().getId())) {
            User newClient = userRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new CustomException("Клиент не найден"));
            existingVehicle.setClient(newClient);
        }

        // Обновляем поля
        existingVehicle.setLicensePlate(dto.getLicensePlate());
        existingVehicle.setModel(dto.getModel());
        existingVehicle.setVehicleType(dto.getVehicleType());
        existingVehicle.setYearOfManufacture(dto.getYearOfManufacture());
        existingVehicle.setCapacity(dto.getCapacity());

        Vehicle updatedVehicle = vehicleRepository.save(existingVehicle);
        log.info("Транспорт обновлен: {}", updatedVehicle.getLicensePlate());

        return vehicleMapper.toDto(updatedVehicle);
    }

    // DELETE
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new CustomException("Транспорт не найден"));

        vehicleRepository.delete(vehicle);
        log.info("Транспорт удален: {}", vehicle.getLicensePlate());
    }

    // VALIDATION
    private void validateVehicle(VehicleDTO dto) {
        if (dto.getClientId() == null) {
            throw new CustomException("ID клиента обязателен");
        }

        if (dto.getLicensePlate() == null || dto.getLicensePlate().trim().isEmpty()) {
            throw new CustomException("Госномер обязателен");
        }
    }

    // STATISTICS
    @Transactional(readOnly = true)
    public long getVehiclesCountByClient(Long clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Клиент не найден"));
        return vehicleRepository.countByClient(client);
    }

    @Transactional(readOnly = true)
    public long getTrucksCountByClient(Long clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Клиент не найден"));
        return vehicleRepository.countTrucksByClient(client);
    }

    @Transactional(readOnly = true)
    public double getTotalCapacityByClient(Long clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Клиент не найден"));
        return vehicleRepository.getTotalCapacityByClient(client);
    }

    // UTILITY METHODS
    @Transactional(readOnly = true)
    public boolean vehicleExists(Long id) {
        return vehicleRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean licensePlateExists(String licensePlate) {
        return vehicleRepository.existsByLicensePlate(licensePlate);
    }
    public boolean isVehicleOwner(Long clientId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new CustomException("Пользователь не найден"));

        return currentUser.getId().equals(clientId);
    }

    public boolean isSingleVehicleOwner(Long vehicleId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new CustomException("Транспорт не найден"));

        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new CustomException("Пользователь не найден"));

        return vehicle.getClient().getId().equals(currentUser.getId());
    }

    // RENTAL METHODS
    @Transactional(readOnly = true)
    public List<VehicleDTO> getAvailableVehicles() {
        // Используем метод, который учитывает NULL значения
        return vehicleRepository.findAvailableVehicles().stream()
                .map(vehicleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VehicleDTO> getRentedVehiclesByDriver(Long driverId) {
        return vehicleRepository.findByDriverId(driverId).stream()
                .map(vehicleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VehicleDTO> getAllRentedVehicles() {
        // Получаем все машины, которые арендованы (driver != null)
        return vehicleRepository.findAll().stream()
                .filter(vehicle -> vehicle.getDriver() != null)
                .map(vehicleMapper::toDto)
                .collect(Collectors.toList());
    }

    public VehicleDTO rentVehicle(Long vehicleId, Long driverId, LocalDateTime endDate) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new CustomException("Транспорт не найден"));

        // Проверяем, что машина доступна (isAvailable = true или NULL) и не арендована (driver = NULL)
        if (vehicle.getIsAvailable() != null && !vehicle.getIsAvailable()) {
            throw new CustomException("Транспорт уже арендован");
        }
        
        if (vehicle.getDriver() != null) {
            throw new CustomException("Транспорт уже арендован другим водителем");
        }

        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new CustomException("Водитель не найден"));

        if (driver.getRole() != com.example.curs4.entity.Role.DRIVER) {
            throw new CustomException("Пользователь не является водителем");
        }

        vehicle.setDriver(driver);
        vehicle.setIsAvailable(false);
        vehicle.setRentalStartDate(LocalDateTime.now());
        vehicle.setRentalEndDate(endDate);

        Vehicle rentedVehicle = vehicleRepository.save(vehicle);
        log.info("Транспорт {} арендован водителем {}", rentedVehicle.getLicensePlate(), driver.getUsername());

        return vehicleMapper.toDto(rentedVehicle);
    }

    public VehicleDTO returnVehicle(Long vehicleId, Long driverId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new CustomException("Транспорт не найден"));

        if (vehicle.getDriver() == null || !vehicle.getDriver().getId().equals(driverId)) {
            throw new CustomException("Вы не арендовали этот транспорт");
        }

        vehicle.setDriver(null);
        vehicle.setIsAvailable(true);
        vehicle.setRentalStartDate(null);
        vehicle.setRentalEndDate(null);

        Vehicle returnedVehicle = vehicleRepository.save(vehicle);
        log.info("Транспорт {} возвращен водителем", returnedVehicle.getLicensePlate());

        return vehicleMapper.toDto(returnedVehicle);
    }
}