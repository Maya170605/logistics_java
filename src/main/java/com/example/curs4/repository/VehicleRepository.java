package com.example.curs4.repository;

import com.example.curs4.entity.User;
import com.example.curs4.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByClient(User client);

    List<Vehicle> findByClientId(Long clientId);

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    boolean existsByLicensePlate(String licensePlate);

    List<Vehicle> findByVehicleTypeContainingIgnoreCase(String vehicleType);

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.client = :client AND LOWER(v.vehicleType) LIKE '%груз%'")
    long countTrucksByClient(@Param("client") User client);

    @Query("SELECT COALESCE(SUM(v.capacity), 0) FROM Vehicle v WHERE v.client = :client")
    double getTotalCapacityByClient(@Param("client") User client);

    long countByClient(User client);

    // Методы для аренды
    @Query("SELECT v FROM Vehicle v WHERE (v.isAvailable = true OR v.isAvailable IS NULL) AND v.driver IS NULL")
    List<Vehicle> findAvailableVehicles(); // Доступные для аренды машины (isAvailable = true или NULL, и driver = NULL)

    List<Vehicle> findByIsAvailableTrue(); // Доступные для аренды машины (только isAvailable = true)

    List<Vehicle> findByDriver(User driver); // Машины, арендованные водителем

    List<Vehicle> findByDriverId(Long driverId); // Машины по ID водителя
}