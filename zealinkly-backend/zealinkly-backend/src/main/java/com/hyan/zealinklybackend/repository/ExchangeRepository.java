package com.hyan.zealinklybackend.repository;

import com.hyan.zealinklybackend.entity.Exchange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
    
    Page<Exchange> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    List<Exchange> findByVolunteerIdOrderByCreatedAtDesc(Long volunteerId);
    
    List<Exchange> findByProductIdOrderByCreatedAtDesc(Long productId);
    
    @Query("SELECT e FROM Exchange e LEFT JOIN FETCH e.volunteer LEFT JOIN FETCH e.product LEFT JOIN FETCH e.admin WHERE e.id = :id")
    Optional<Exchange> findByIdWithAssociations(@Param("id") Long id);
}
