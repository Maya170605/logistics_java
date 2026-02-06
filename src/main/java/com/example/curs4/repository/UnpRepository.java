package com.example.curs4.repository;

import com.example.curs4.entity.Unp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UnpRepository extends JpaRepository<Unp, Long> {
    Optional<Unp> findByUnp(String unp);

}
