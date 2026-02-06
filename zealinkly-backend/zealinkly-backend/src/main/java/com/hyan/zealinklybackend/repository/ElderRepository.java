package com.hyan.zealinklybackend.repository;

import com.hyan.zealinklybackend.entity.Elder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ElderRepository extends JpaRepository<Elder, Long> {
    Optional<Elder> findByUsername(String username);
    boolean existsByUsername(String username);
}
