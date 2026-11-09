package com.example.campussystem.controller;

import com.example.campussystem.common.ApiResponse;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.security.UserPrincipal;
import com.example.campussystem.service.FileUploadService;
import com.example.campussystem.service.SystemLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 文件上传控制器（本地 / 阿里云 OSS）
 */
@RestController
@RequestMapping("/files")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private SystemLogService systemLogService;

    /**
     * 上传单个图片
     */
    @PostMapping("/upload/image")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "products") String category,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {

        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String filePath = fileUploadService.uploadImage(file, category);

        systemLogService.logOperation(
                userPrincipal.getUserId().toString(),
                "上传图片",
                "分类: " + category + ", 文件: " + file.getOriginalFilename()
        );

        String fileUrl = fileUploadService.getFileUrl(filePath);

        Map<String, Object> result = new HashMap<>();
        result.put("filePath", filePath);
        result.put("fileName", file.getOriginalFilename());
        result.put("fileSize", file.getSize());
        result.put("url", fileUrl);

        return ResponseEntity.ok(ApiResponse.success(result, "图片上传成功"));
    }

    /**
     * 批量上传图片
     */
    @PostMapping("/upload/images")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> uploadImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(defaultValue = "products") String category,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {

        List<Map<String, Object>> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String filePath = fileUploadService.uploadImage(file, category);
                String fileUrl = fileUploadService.getFileUrl(filePath);

                Map<String, Object> result = new HashMap<>();
                result.put("filePath", filePath);
                result.put("fileName", file.getOriginalFilename());
                result.put("fileSize", file.getSize());
                result.put("url", fileUrl);
                result.put("success", true);
                results.add(result);

            } catch (Exception e) {
                Map<String, Object> result = new HashMap<>();
                result.put("fileName", file.getOriginalFilename());
                result.put("error", e.getMessage());
                result.put("success", false);
                results.add(result);
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        systemLogService.logOperation(
                userPrincipal.getUserId().toString(),
                "批量上传图片",
                "分类: " + category + ", 文件数量: " + files.length + ", 成功: " + (files.length - errors.size())
        );

        if (errors.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(results, "所有图片上传成功"));
        } else {
            return ResponseEntity.ok(ApiResponse.success(results, "部分图片上传成功"));
        }
    }

    /**
     * 上传普通文件
     */
    @PostMapping("/upload/file")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "documents") String category,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {

        String filePath = fileUploadService.uploadFile(file, category);

        systemLogService.logOperation(
                userPrincipal.getUserId().toString(),
                "上传文件",
                "分类: " + category + ", 文件: " + file.getOriginalFilename()
        );

        String fileUrl = fileUploadService.getFileUrl(filePath);

        Map<String, Object> result = new HashMap<>();
        result.put("filePath", filePath);
        result.put("fileName", file.getOriginalFilename());
        result.put("fileSize", file.getSize());
        result.put("url", fileUrl);

        return ResponseEntity.ok(ApiResponse.success(result, "文件上传成功"));
    }

    /**
     * 查看/预览文件：OSS 模式重定向到对象地址，本地模式读磁盘
     */
    @GetMapping("/view/**")
    public ResponseEntity<?> viewFile(HttpServletRequest request) {
        try {
            String requestURI = request.getRequestURI();
            String objectName;

            if (requestURI.contains("/api/v1/files/view/")) {
                objectName = requestURI.substring(requestURI.indexOf("/api/v1/files/view/") + "/api/v1/files/view/".length());
            } else if (requestURI.contains("/files/view/")) {
                objectName = requestURI.substring(requestURI.indexOf("/files/view/") + "/files/view/".length());
            } else {
                return ResponseEntity.notFound().build();
            }

            if (objectName.contains("?")) {
                objectName = objectName.substring(0, objectName.indexOf("?"));
            }

            String fileUrl = fileUploadService.getFileUrl(objectName);
            if (fileUrl != null && (fileUrl.startsWith("http://") || fileUrl.startsWith("https://"))) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(fileUrl))
                        .build();
            }

            Path filePath = fileUploadService.getAbsolutePath(objectName);
            if (!Files.exists(filePath)) {
                logger.warn("文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            InputStream inputStream = Files.newInputStream(filePath);
            Resource resource = new InputStreamResource(inputStream);
            String contentType = determineContentType(objectName);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                    .body(resource);

        } catch (Exception e) {
            logger.error("查看文件失败: {}", request.getRequestURI(), e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 下载文件（从本地磁盘读取）
     */
    @GetMapping("/download/**")
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request) {
        try {
            String requestURI = request.getRequestURI();
            String objectName;

            if (requestURI.contains("/api/v1/files/download/")) {
                objectName = requestURI.substring(requestURI.indexOf("/api/v1/files/download/") + "/api/v1/files/download/".length());
            } else if (requestURI.contains("/files/download/")) {
                objectName = requestURI.substring(requestURI.indexOf("/files/download/") + "/files/download/".length());
            } else {
                return ResponseEntity.notFound().build();
            }

            if (objectName.contains("?")) {
                objectName = objectName.substring(0, objectName.indexOf("?"));
            }

            Path filePath = fileUploadService.getAbsolutePath(objectName);
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            InputStream inputStream = Files.newInputStream(filePath);
            Resource resource = new InputStreamResource(inputStream);

            String fileName = objectName.substring(objectName.lastIndexOf("/") + 1);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{category}/{fileName}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable String category,
            @PathVariable String fileName,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        String filePath = category + "/" + fileName;
        boolean deleted = fileUploadService.deleteFile(filePath);

        if (deleted) {
            systemLogService.logOperation(
                    userPrincipal.getUserId().toString(),
                    "删除文件",
                    "文件路径: " + filePath
            );
            return ResponseEntity.ok(ApiResponse.success("文件删除成功"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("文件删除失败"));
        }
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/info/{category}/{fileName}")
    public ResponseEntity<ApiResponse<FileUploadService.FileInfo>> getFileInfo(
            @PathVariable String category,
            @PathVariable String fileName) {

        String filePath = category + "/" + fileName;
        FileUploadService.FileInfo fileInfo = fileUploadService.getFileInfo(filePath);

        if (fileInfo != null) {
            return ResponseEntity.ok(ApiResponse.success(fileInfo, "获取文件信息成功"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 确定文件内容类型
     */
    private String determineContentType(String filePath) {
        if (filePath == null) return "application/octet-stream";
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex == -1) return "application/octet-stream";

        String extension = filePath.substring(dotIndex + 1).toLowerCase();
        switch (extension) {
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "svg": return "image/svg+xml";
            case "webp": return "image/webp";
            case "bmp": return "image/bmp";
            case "pdf": return "application/pdf";
            case "txt": return "text/plain";
            default: return "application/octet-stream";
        }
    }
}
