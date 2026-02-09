package com.hyan.zealinklybackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 紧急联系人实体
 */
@Entity
@Table(name = "emergency_contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elder_id", insertable = false, updatable = false)
    private Elder elder;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 30)
    private String relation;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(columnDefinition = "INT DEFAULT 1")
    private Integer priority = 1;
}
