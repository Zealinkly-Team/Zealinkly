package com.hyan.zealinklybackend.repository;

import com.hyan.zealinklybackend.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 紧急联系人Repository
 */
@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {
    
    /**
     * 根据老人ID查询紧急联系人列表，按优先级排序
     */
    List<EmergencyContact> findByElderIdOrderByPriorityAsc(Long elderId);
}
