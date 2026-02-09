package com.hyan.zealinklybackend.controller;

import com.hyan.zealinklybackend.dto.response.ApiResponse;
import com.hyan.zealinklybackend.dto.response.FileUploadResponse;
import com.hyan.zealinklybackend.entity.FileStorage;
import com.hyan.zealinklybackend.security.UserPrincipal;
import com.hyan.zealinklybackend.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件上传和管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 上传文件（MultipartFile）
     */
    @PostMapping("/upload")
    public ApiResponse<FileUploadResponse> uploadFile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "relatedType", required = false) String relatedType,
            @RequestParam(value = "relatedId", required = false) Long relatedId) {
        
        try {
            FileStorage fileStorage = fileService.uploadFile(
                    file,
                    principal.getUserType(),
                    principal.getUserId(),
                    relatedType,
                    relatedId
            );
            
            FileUploadResponse response = FileUploadResponse.builder()
                    .id(fileStorage.getId())
                    .fileUrl(fileStorage.getFileUrl())
                    .originalFilename(fileStorage.getOriginalFilename())
                    .fileSize(fileStorage.getFileSize())
                    .contentType(fileStorage.getContentType())
                    .build();
            
            return ApiResponse.success("文件上传成功", response);
            
        } catch (IOException e) {
            log.error("File upload failed", e);
            return ApiResponse.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件（Base64编码）
     */
    @PostMapping("/upload-base64")
    public ApiResponse<FileUploadResponse> uploadFileFromBase64(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody FileBase64UploadRequest request) {
        
        try {
            FileStorage fileStorage = fileService.uploadFileFromBase64(
                    request.getBase64Data(),
                    request.getFilename(),
                    request.getContentType(),
                    principal.getUserType(),
                    principal.getUserId(),
                    request.getRelatedType(),
                    request.getRelatedId()
            );
            
            FileUploadResponse response = FileUploadResponse.builder()
                    .id(fileStorage.getId())
                    .fileUrl(fileStorage.getFileUrl())
                    .originalFilename(fileStorage.getOriginalFilename())
                    .fileSize(fileStorage.getFileSize())
                    .contentType(fileStorage.getContentType())
                    .build();
            
            return ApiResponse.success("文件上传成功", response);
            
        } catch (IOException e) {
            log.error("File upload from base64 failed", e);
            return ApiResponse.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件（下载）
     */
    @GetMapping("/{type}/{year}/{month}/{day}/{filename:.+}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String type,
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String day,
            @PathVariable String filename) {
        
        try {
            String storedFilename = String.format("%s/%s/%s/%s/%s", type, year, month, day, filename);
            Path filePath = fileService.getFilePath(storedFilename);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            
            // 确定Content-Type
            String contentType = "application/octet-stream";
            try {
                contentType = java.nio.file.Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
            } catch (IOException e) {
                log.warn("Failed to determine content type", e);
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
            
        } catch (Exception e) {
            log.error("File retrieval failed", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取我的文件列表
     */
    @GetMapping("/my")
    public ApiResponse<List<FileUploadResponse>> getMyFiles(
            @AuthenticationPrincipal UserPrincipal principal) {
        
        List<FileStorage> files = fileService.getFilesByUploader(
                principal.getUserType(),
                principal.getUserId()
        );
        
        List<FileUploadResponse> responses = files.stream()
                .map(file -> FileUploadResponse.builder()
                        .id(file.getId())
                        .fileUrl(file.getFileUrl())
                        .originalFilename(file.getOriginalFilename())
                        .fileSize(file.getFileSize())
                        .contentType(file.getContentType())
                        .fileType(file.getFileType())
                        .createdAt(file.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        return ApiResponse.success(responses);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{fileId}")
    public ApiResponse<Void> deleteFile(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long fileId) {
        
        FileStorage fileStorage = fileService.getFile(fileId);
        
        // 检查权限：只能删除自己上传的文件
        if (!fileStorage.getUploaderType().equals(principal.getUserType()) ||
            !fileStorage.getUploaderId().equals(principal.getUserId())) {
            return ApiResponse.error(403, "无权删除此文件");
        }
        
        fileService.deleteFile(fileId);
        
        return ApiResponse.success("文件删除成功", null);
    }

    /**
     * Base64上传请求
     */
    @lombok.Data
    public static class FileBase64UploadRequest {
        private String base64Data;
        private String filename;
        private String contentType;
        private String relatedType;
        private Long relatedId;
    }
}
