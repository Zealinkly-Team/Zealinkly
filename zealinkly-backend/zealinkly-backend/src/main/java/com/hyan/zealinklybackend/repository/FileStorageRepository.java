package com.hyan.zealinklybackend.repository;

import com.hyan.zealinklybackend.entity.FileStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileStorageRepository extends JpaRepository<FileStorage, Long> {
    
    /**
     * 根据上传者查询文件
     */
    List<FileStorage> findByUploaderTypeAndUploaderIdOrderByCreatedAtDesc(String uploaderType, Long uploaderId);
    
    /**
     * 根据关联业务查询文件
     */
    List<FileStorage> findByRelatedTypeAndRelatedIdOrderByCreatedAtDesc(String relatedType, Long relatedId);
    
    /**
     * 根据存储文件名查询
     */
    Optional<FileStorage> findByStoredFilename(String storedFilename);
    
    /**
     * 根据文件URL查询
     */
    Optional<FileStorage> findByFileUrl(String fileUrl);
}
