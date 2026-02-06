package com.example.curs4.repository;

import com.example.curs4.entity.Activity;
import com.example.curs4.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    void deleteByUserId(Long userId);

    List<Activity> findByUserOrderByActivityDateDesc(User user);

    List<Activity> findByUserIdOrderByActivityDateDesc(Long userId);

    Page<Activity> findByUserOrderByActivityDateDesc(User user, Pageable pageable);

    Page<Activity> findByUserIdOrderByActivityDateDesc(Long userId, Pageable pageable);

    List<Activity> findTop5ByUserOrderByActivityDateDesc(User user);

    List<Activity> findTop5ByUserIdOrderByActivityDateDesc(Long userId);

    List<Activity> findByActivityDateBetweenOrderByActivityDateDesc(
            LocalDateTime startDate, LocalDateTime endDate);

    List<Activity> findByUserAndActivityDateBetweenOrderByActivityDateDesc(
            User user, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(a) FROM Activity a WHERE a.user = :user")
    long countByUser(@Param("user") User user);

    @Query("SELECT a FROM Activity a WHERE a.user = :user AND LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY a.activityDate DESC")
    List<Activity> findByUserAndDescriptionContainingIgnoreCase(
            @Param("user") User user,
            @Param("keyword") String keyword);

}