package com.hyan.zealinklybackend.repository;

import com.hyan.zealinklybackend.entity.TaskEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskEvidenceRepository extends JpaRepository<TaskEvidence, Long> {

    List<TaskEvidence> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}
