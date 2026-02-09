package com.hyan.zealinklybackend.repository;

import com.hyan.zealinklybackend.entity.Elder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ElderRepository extends JpaRepository<Elder, Long> {
    Optional<Elder> findByUsername(String username);
    boolean existsByUsername(String username);
    Page<Elder> findAll(Pageable pageable);
    Page<Elder> findByEnabled(Boolean enabled, Pageable pageable);
    List<Elder> findByEnabledTrue();
    Optional<Elder> findByIdCardNumber(String idCardNumber);
    Optional<Elder> findByCommunityCardNumber(String communityCardNumber);
}
