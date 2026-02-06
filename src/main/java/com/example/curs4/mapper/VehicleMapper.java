package com.example.curs4.mapper;

import com.example.curs4.dto.VehicleDTO;
import com.example.curs4.entity.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    public VehicleDTO toDto(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        String clientName = null;
        if (vehicle.getClient() != null) {
            clientName = vehicle.getClient().getName();
            if (clientName == null) {
                clientName = vehicle.getClient().getUsername();
            }
        }

        String driverName = null;
        if (vehicle.getDriver() != null) {
            driverName = vehicle.getDriver().getName();
            if (driverName == null) {
                driverName = vehicle.getDriver().getUsername();
            }
        }

        return VehicleDTO.builder()
                .id(vehicle.getId())
                .licensePlate(vehicle.getLicensePlate())
                .model(vehicle.getModel())
                .vehicleType(vehicle.getVehicleType())
                .yearOfManufacture(vehicle.getYearOfManufacture())
                .capacity(vehicle.getCapacity())
                .clientId(vehicle.getClient() != null ? vehicle.getClient().getId() : null)
                .clientName(clientName)
                .driverId(vehicle.getDriver() != null ? vehicle.getDriver().getId() : null)
                .driverName(driverName)
                .isAvailable(vehicle.getDriver() == null && (vehicle.getIsAvailable() == null || vehicle.getIsAvailable()))
                .rentalStartDate(vehicle.getRentalStartDate())
                .rentalEndDate(vehicle.getRentalEndDate())
                .createdAt(vehicle.getCreatedAt())
                //.updatedAt(vehicle.getUpdatedAt())
                .build();
    }

    public Vehicle toEntity(VehicleDTO dto) {
        if (dto == null) {
            return null;
        }

        return Vehicle.builder()
                .id(dto.getId())
                .licensePlate(dto.getLicensePlate())
                .model(dto.getModel())
                .vehicleType(dto.getVehicleType())
                .yearOfManufacture(dto.getYearOfManufacture())
                .capacity(dto.getCapacity())
                .build();
    }
}