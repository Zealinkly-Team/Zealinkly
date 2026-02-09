package com.hyan.zealinklybackend.repository;

import com.hyan.zealinklybackend.entity.Appeal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppealRepository extends JpaRepository<Appeal, Long> {

    List<Appeal> findByStatusOrderByCreatedAtDesc(String status);

    Page<Appeal> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    Page<Appeal> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Appeal> findByTaskId(Long taskId);
}
