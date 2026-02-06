package com.example.curs4.repository;

import com.example.curs4.entity.Declaration;
import com.example.curs4.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeclarationRepository extends JpaRepository<Declaration, Long> {

    List<Declaration> findByClient(User client);

    List<Declaration> findByClientId(Long clientId);

    List<Declaration> findByStatus(String status);

    Optional<Declaration> findByDeclarationNumber(String declarationNumber);

    boolean existsByDeclarationNumber(String declarationNumber);

    long countByClient(User client);

    long countByClientAndStatus(User client, String status);
}