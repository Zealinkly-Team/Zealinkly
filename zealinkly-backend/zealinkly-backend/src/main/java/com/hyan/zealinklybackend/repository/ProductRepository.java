package com.hyan.zealinklybackend.repository;

import com.hyan.zealinklybackend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Page<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Page<Product> findByEnabledTrueOrderByCreatedAtDesc(Pageable pageable);
    
    List<Product> findByEnabledTrue();
    
    Optional<Product> findByIdAndEnabledTrue(Long id);
}
