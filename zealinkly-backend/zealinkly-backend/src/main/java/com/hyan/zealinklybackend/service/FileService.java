package com.hyan.zealinklybackend.service;

import com.hyan.zealinklybackend.entity.FileStorage;
import com.hyan.zealinklybackend.exception.BusinessException;
import com.hyan.zealinklybackend.repository.FileStorageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * 文件服务：处理文件上传、存储、访问
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileStorageRepository fileStorageRepository;

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.base-url:http://localhost:8080/api/files}")
    private String baseUrl;

    @Value("${app.file.max-size:10485760}") // 默认10MB
    private Long maxFileSize;

    /**
     * 文件类型枚举
     */
    public enum FileType {
        IMAGE("image", new String[]{"jpg", "jpeg", "png", "gif", "bmp", "webp"}),
        AUDIO("audio", new String[]{"wav", "mp3", "amr", "m4a", "pcm"}),
        DOCUMENT("document", new String[]{"pdf", "doc", "docx", "xls", "xlsx", "txt"});

        private final String type;
        private final String[] allowedExtensions;

        FileType(String type, String[] allowedExtensions) {
            this.type = type;
            this.allowedExtensions = allowedExtensions;
        }

        public String getType() {
            return type;
        }

        public String[] getAllowedExtensions() {
            return allowedExtensions;
        }

        public static FileType fromExtension(String extension) {
            String ext = extension.toLowerCase();
            for (FileType type : values()) {
                for (String allowed : type.allowedExtensions) {
                    if (allowed.equals(ext)) {
                        return type;
                    }
                }
            }
            throw new BusinessException("不支持的文件类型: " + extension);
        }
    }

    /**
     * 上传文件（MultipartFile）
     */
    @Transactional
    public FileStorage uploadFile(
            MultipartFile file,
            String uploaderType,
            Long uploaderId,
            String relatedType,
            Long relatedId) throws IOException {
        
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            throw new BusinessException("文件大小不能超过 " + (maxFileSize / 1024 / 1024) + "MB");
        }

        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        
        // 确定文件类型
        FileType fileType = FileType.fromExtension(extension);
        
        // 生成存储路径和文件名
        String storedPath = generateStoredPath(fileType.getType());
        String storedFilename = generateStoredFilename(extension);
        String fullPath = storedPath + "/" + storedFilename;
        
        // 创建目录
        Path uploadPath = Paths.get(uploadDir, storedPath);
        Files.createDirectories(uploadPath);
        
        // 保存文件
        Path filePath = uploadPath.resolve(storedFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // 生成访问URL
        String fileUrl = baseUrl + "/" + storedPath + "/" + storedFilename;
        
        // 保存文件元数据
        FileStorage fileStorage = FileStorage.builder()
                .fileType(fileType.getType())
                .originalFilename(originalFilename)
                .storedFilename(fullPath)
                .fileUrl(fileUrl)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .fileExtension(extension)
                .uploaderType(uploaderType)
                .uploaderId(uploaderId)
                .relatedType(relatedType)
                .relatedId(relatedId)
                .build();
        
        fileStorage = fileStorageRepository.save(fileStorage);
        
        log.info("File uploaded: {} -> {}", originalFilename, fileUrl);
        
        return fileStorage;
    }

    /**
     * 上传文件（Base64编码）
     */
    @Transactional
    public FileStorage uploadFileFromBase64(
            String base64Data,
            String filename,
            String contentType,
            String uploaderType,
            Long uploaderId,
            String relatedType,
            Long relatedId) throws IOException {
        
        // 解析base64数据
        String base64 = base64Data.trim();
        if (base64.contains(",")) {
            base64 = base64.substring(base64.indexOf(",") + 1);
        }
        base64 = base64.replaceAll("\\s+", "");
        
        byte[] fileBytes = Base64.getDecoder().decode(base64);
        
        // 检查文件大小
        if (fileBytes.length > maxFileSize) {
            throw new BusinessException("文件大小不能超过 " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        // 获取文件扩展名
        String extension = getFileExtension(filename);
        if (extension == null || extension.isEmpty()) {
            // 尝试从contentType推断扩展名
            extension = getExtensionFromContentType(contentType);
        }
        
        // 确定文件类型
        FileType fileType = FileType.fromExtension(extension);
        
        // 生成存储路径和文件名
        String storedPath = generateStoredPath(fileType.getType());
        String storedFilename = generateStoredFilename(extension);
        String fullPath = storedPath + "/" + storedFilename;
        
        // 创建目录
        Path uploadPath = Paths.get(uploadDir, storedPath);
        Files.createDirectories(uploadPath);
        
        // 保存文件
        Path filePath = uploadPath.resolve(storedFilename);
        Files.write(filePath, fileBytes);
        
        // 生成访问URL
        String fileUrl = baseUrl + "/" + storedPath + "/" + storedFilename;
        
        // 保存文件元数据
        FileStorage fileStorage = FileStorage.builder()
                .fileType(fileType.getType())
                .originalFilename(filename)
                .storedFilename(fullPath)
                .fileUrl(fileUrl)
                .fileSize((long) fileBytes.length)
                .contentType(contentType)
                .fileExtension(extension)
                .uploaderType(uploaderType)
                .uploaderId(uploaderId)
                .relatedType(relatedType)
                .relatedId(relatedId)
                .build();
        
        fileStorage = fileStorageRepository.save(fileStorage);
        
        log.info("File uploaded from base64: {} -> {}", filename, fileUrl);
        
        return fileStorage;
    }

    /**
     * 获取文件
     */
    public FileStorage getFile(Long fileId) {
        return fileStorageRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException("文件不存在"));
    }

    /**
     * 获取文件路径（用于读取文件）
     */
    public Path getFilePath(String storedFilename) {
        Path filePath = Paths.get(uploadDir, storedFilename);
        if (!Files.exists(filePath)) {
            throw new BusinessException("文件不存在");
        }
        return filePath;
    }

    /**
     * 删除文件
     */
    @Transactional
    public void deleteFile(Long fileId) {
        FileStorage fileStorage = getFile(fileId);
        
        // 删除物理文件
        try {
            Path filePath = Paths.get(uploadDir, fileStorage.getStoredFilename());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete physical file: {}", fileStorage.getStoredFilename(), e);
        }
        
        // 删除数据库记录
        fileStorageRepository.delete(fileStorage);
        
        log.info("File deleted: {}", fileStorage.getFileUrl());
    }

    /**
     * 根据上传者查询文件
     */
    public List<FileStorage> getFilesByUploader(String uploaderType, Long uploaderId) {
        return fileStorageRepository.findByUploaderTypeAndUploaderIdOrderByCreatedAtDesc(uploaderType, uploaderId);
    }

    /**
     * 根据关联业务查询文件
     */
    public List<FileStorage> getFilesByRelated(String relatedType, Long relatedId) {
        return fileStorageRepository.findByRelatedTypeAndRelatedIdOrderByCreatedAtDesc(relatedType, relatedId);
    }

    /**
     * 生成存储路径：{type}/{year}/{month}/{day}
     */
    private String generateStoredPath(String fileType) {
        LocalDate now = LocalDate.now();
        return String.format("%s/%d/%02d/%02d",
                fileType,
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth());
    }

    /**
     * 生成存储文件名：{timestamp}_{random}.{ext}
     */
    private String generateStoredFilename(String extension) {
        long timestamp = System.currentTimeMillis();
        String random = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%d_%s.%s", timestamp, random, extension.toLowerCase());
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return null;
        }
        return filename.substring(lastDot + 1).toLowerCase();
    }

    /**
     * 从ContentType推断扩展名
     */
    private String getExtensionFromContentType(String contentType) {
        if (contentType == null) {
            return "bin";
        }
        return switch (contentType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "audio/wav", "audio/wave" -> "wav";
            case "audio/mpeg", "audio/mp3" -> "mp3";
            case "audio/amr" -> "amr";
            case "audio/mp4", "audio/m4a" -> "m4a";
            default -> "bin";
        };
    }
}
