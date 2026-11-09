package com.example.campussystem.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;

public interface FileUploadService {

    String uploadImage(MultipartFile file, String category);

    String uploadFile(MultipartFile file, String category);

    boolean deleteFile(String objectName);

    FileInfo getFileInfo(String objectName);

    String getFileUrl(String objectName);

    Path getAbsolutePath(String relativePath);

    class FileInfo {
        private String fileName;
        private String filePath;
        private long fileSize;
        private LocalDateTime lastModified;
        private Integer width;
        private Integer height;

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        public LocalDateTime getLastModified() { return lastModified; }
        public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
        public Integer getWidth() { return width; }
        public void setWidth(Integer width) { this.width = width; }
        public Integer getHeight() { return height; }
        public void setHeight(Integer height) { this.height = height; }
    }
}
