package com.hyan.zealinklybackend.repository;

import com.hyan.zealinklybackend.entity.Task;
import com.hyan.zealinklybackend.entity.TaskStatus;
import com.hyan.zealinklybackend.entity.TaskType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.elder LEFT JOIN FETCH t.volunteer LEFT JOIN FETCH t.admin WHERE t.id = :id")
    Optional<Task> findByIdWithAssociations(@Param("id") Long id);

    Page<Task> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Task> findByTaskTypeAndStatus(TaskType taskType, TaskStatus status);

    List<Task> findByTaskTypeAndStatusOrderByCreatedAtDesc(TaskType taskType, TaskStatus status);

    List<Task> findByTaskTypeAndElderIdOrderByCreatedAtDesc(TaskType taskType, Long elderId);

    List<Task> findByTaskTypeAndVolunteerIdOrderByCreatedAtDesc(TaskType taskType, Long volunteerId);

    List<Task> findByTaskTypeAndElderId(TaskType taskType, Long elderId);

    List<Task> findByTaskTypeOrderByCreatedAtDesc(TaskType taskType);
}
