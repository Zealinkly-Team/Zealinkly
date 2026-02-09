package com.hyan.zealinklybackend.repository;

import com.hyan.zealinklybackend.entity.Volunteer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
    Optional<Volunteer> findByUsername(String username);
    boolean existsByUsername(String username);
    Page<Volunteer> findAll(Pageable pageable);
    Page<Volunteer> findByEnabled(Boolean enabled, Pageable pageable);
    Optional<Volunteer> findByIdCardNumber(String idCardNumber);
    Optional<Volunteer> findByCommunityCardNumber(String communityCardNumber);
}
