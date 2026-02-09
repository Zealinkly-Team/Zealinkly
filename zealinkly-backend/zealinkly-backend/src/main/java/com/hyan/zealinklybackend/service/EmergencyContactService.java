package com.hyan.zealinklybackend.service;

import com.hyan.zealinklybackend.dto.request.EmergencyContactCreateRequest;
import com.hyan.zealinklybackend.dto.request.EmergencyContactUpdateRequest;
import com.hyan.zealinklybackend.dto.response.EmergencyContactResponse;
import com.hyan.zealinklybackend.entity.Elder;
import com.hyan.zealinklybackend.entity.EmergencyContact;
import com.hyan.zealinklybackend.exception.BusinessException;
import com.hyan.zealinklybackend.repository.ElderRepository;
import com.hyan.zealinklybackend.repository.EmergencyContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 紧急联系人服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyContactService {

    private final EmergencyContactRepository emergencyContactRepository;
    private final ElderRepository elderRepository;

    /**
     * 创建紧急联系人（老人自己添加）
     */
    @Transactional
    public EmergencyContactResponse create(Long elderId, EmergencyContactCreateRequest request) {
        // 验证老人存在
        if (!elderRepository.existsById(elderId)) {
            throw new BusinessException("老人不存在");
        }
        
        // 检查是否已存在相同电话的联系人
        emergencyContactRepository.findByElderIdOrderByPriorityAsc(elderId).stream()
                .filter(contact -> contact.getPhone().equals(request.getPhone()))
                .findFirst()
                .ifPresent(contact -> {
                    throw new BusinessException("该电话号码已存在");
                });
        
        EmergencyContact contact = new EmergencyContact();
        contact.setElderId(elderId);
        contact.setName(request.getName());
        contact.setRelation(request.getRelation());
        contact.setPhone(request.getPhone());
        contact.setPriority(request.getPriority() != null ? request.getPriority() : 1);
        
        contact = emergencyContactRepository.save(contact);
        
        return EmergencyContactResponse.builder()
                .id(contact.getId())
                .name(contact.getName())
                .relation(contact.getRelation())
                .phone(contact.getPhone())
                .priority(contact.getPriority())
                .build();
    }

    /**
     * 管理员为老人创建紧急联系人
     */
    @Transactional
    public EmergencyContactResponse createByAdmin(Long elderId, EmergencyContactCreateRequest request) {
        // 验证老人存在
        if (!elderRepository.existsById(elderId)) {
            throw new BusinessException("老人不存在");
        }
        
        // 检查是否已存在相同电话的联系人
        emergencyContactRepository.findByElderIdOrderByPriorityAsc(elderId).stream()
                .filter(contact -> contact.getPhone().equals(request.getPhone()))
                .findFirst()
                .ifPresent(contact -> {
                    throw new BusinessException("该电话号码已存在");
                });
        
        EmergencyContact contact = new EmergencyContact();
        contact.setElderId(elderId);
        contact.setName(request.getName());
        contact.setRelation(request.getRelation());
        contact.setPhone(request.getPhone());
        contact.setPriority(request.getPriority() != null ? request.getPriority() : 1);
        
        contact = emergencyContactRepository.save(contact);
        
        return EmergencyContactResponse.builder()
                .id(contact.getId())
                .name(contact.getName())
                .relation(contact.getRelation())
                .phone(contact.getPhone())
                .priority(contact.getPriority())
                .build();
    }

    /**
     * 获取老人的紧急联系人列表
     */
    @Transactional(readOnly = true)
    public List<EmergencyContactResponse> getByElderId(Long elderId) {
        List<EmergencyContact> contacts = emergencyContactRepository.findByElderIdOrderByPriorityAsc(elderId);
        return contacts.stream()
                .map(contact -> EmergencyContactResponse.builder()
                        .id(contact.getId())
                        .name(contact.getName())
                        .relation(contact.getRelation())
                        .phone(contact.getPhone())
                        .priority(contact.getPriority())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 更新紧急联系人
     */
    @Transactional
    public EmergencyContactResponse update(Long contactId, Long elderId, EmergencyContactUpdateRequest request) {
        EmergencyContact contact = emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new BusinessException("紧急联系人不存在"));
        
        // 验证联系人属于该老人
        if (!contact.getElderId().equals(elderId)) {
            throw new BusinessException("无权修改此联系人");
        }
        
        // 如果更新电话，检查是否与其他联系人重复
        if (request.getPhone() != null && !request.getPhone().equals(contact.getPhone())) {
            emergencyContactRepository.findByElderIdOrderByPriorityAsc(elderId).stream()
                    .filter(c -> !c.getId().equals(contactId) && c.getPhone().equals(request.getPhone()))
                    .findFirst()
                    .ifPresent(c -> {
                        throw new BusinessException("该电话号码已被其他联系人使用");
                    });
        }
        
        if (request.getName() != null) {
            contact.setName(request.getName());
        }
        if (request.getRelation() != null) {
            contact.setRelation(request.getRelation());
        }
        if (request.getPhone() != null) {
            contact.setPhone(request.getPhone());
        }
        if (request.getPriority() != null) {
            contact.setPriority(request.getPriority());
        }
        
        contact = emergencyContactRepository.save(contact);
        
        return EmergencyContactResponse.builder()
                .id(contact.getId())
                .name(contact.getName())
                .relation(contact.getRelation())
                .phone(contact.getPhone())
                .priority(contact.getPriority())
                .build();
    }

    /**
     * 删除紧急联系人
     */
    @Transactional
    public void delete(Long contactId, Long elderId) {
        EmergencyContact contact = emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new BusinessException("紧急联系人不存在"));
        
        // 验证联系人属于该老人
        if (!contact.getElderId().equals(elderId)) {
            throw new BusinessException("无权删除此联系人");
        }
        
        emergencyContactRepository.delete(contact);
    }

    /**
     * 管理员删除紧急联系人
     */
    @Transactional
    public void deleteByAdmin(Long contactId) {
        EmergencyContact contact = emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new BusinessException("紧急联系人不存在"));
        
        emergencyContactRepository.delete(contact);
    }
}
