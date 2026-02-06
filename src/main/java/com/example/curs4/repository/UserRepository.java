package com.example.curs4.repository;

import com.example.curs4.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.management.relation.Role;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Метод для безопасного удаления с обработкой связей
    @Modifying
    @Query("DELETE FROM User u WHERE u.id = :id")
    @Transactional
    void deleteUserWithRelations(@Param("id") Long id);
    Optional<User> findByUnp_Unp(String unp);
    boolean existsByUnp_Unp(String unp);
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByRole(com.example.curs4.entity.Role role);
    Page<User> findAll(Pageable pageable);
}