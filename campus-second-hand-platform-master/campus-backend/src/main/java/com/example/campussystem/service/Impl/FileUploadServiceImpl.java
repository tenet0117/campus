package com.example.campussystem.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传：支持本地存储与阿里云 OSS（file.storage-type=local|oss）
 */
@Service
public class FileUploadServiceImpl implements FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadServiceImpl.class);

    @Value("${file.upload.path:uploads}")
    private String uploadRootPath;

    @Value("${file.upload.max-size:20971520}")
    private long maxFileSize;

    @Value("${file.storage-type:local}")
    private String storageType;

    @Value("${file.oss.bucket-name:}")
    private String ossBucket;

    @Value("${file.oss.endpoint:}")
    private String ossEndpoint;

    @Value("${file.oss.url-prefix:}")
    private String ossUrlPrefix;

    @Value("${file.oss.dir-prefix:campus/}")
    private String ossDirPrefix;

    @Value("${file.oss.signed-url:false}")
    private boolean ossSignedUrl;

    @Value("${file.oss.signed-expire-seconds:86400}")
    private long ossSignedExpireSeconds;

    @Autowired
    private ObjectProvider<OSS> ossClientProvider;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp"
    );

    private static final int MAX_IMAGE_WIDTH = 1920;
    private static final int MAX_IMAGE_HEIGHT = 1080;

    private boolean useOss() {
        return "oss".equalsIgnoreCase(storageType) && ossClientProvider.getIfAvailable() != null;
    }

    private OSS requireOss() {
        OSS oss = ossClientProvider.getIfAvailable();
        if (oss == null) {
            throw new BusinessException("OSS 未初始化，请检查 file.storage-type 与 file.oss.* 配置");
        }
        if (ossBucket == null || ossBucket.isBlank()) {
            throw new BusinessException("未配置 file.oss.bucket-name");
        }
        return oss;
    }

    private String buildObjectKey(String category, String fileName) {
        String prefix = ossDirPrefix == null ? "" : ossDirPrefix;
        if (!prefix.isEmpty() && !prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        String cat = category == null ? "misc" : category.replace("\\", "/");
        while (cat.startsWith("/")) {
            cat = cat.substring(1);
        }
        return prefix + cat + "/" + fileName;
    }

    @Override
    public String uploadImage(MultipartFile file, String category) {
        validateFile(file);
        validateImageFile(file);
        ensureOssReady();

        try {
            byte[] fileBytes = file.getBytes();
            BufferedImage processedImage = processImage(fileBytes);
            String fileName = generateFileName(file.getOriginalFilename());
            String fileExtension = getFileExtension(file.getOriginalFilename());
            byte[] imageBytes = compressImageOptimized(processedImage, fileBytes, fileExtension);

            if (useOss()) {
                String objectKey = buildObjectKey(category, fileName);
                uploadToOss(objectKey, imageBytes, contentTypeOf(fileExtension));
                logger.info("图片已上传到 OSS: {}", objectKey);
                return objectKey;
            }

            Path categoryDir = Paths.get(uploadRootPath, category);
            Files.createDirectories(categoryDir);
            Path targetPath = categoryDir.resolve(fileName);
            Files.write(targetPath, imageBytes);
            String relativePath = category + "/" + fileName;
            logger.info("图片已保存到本地: {}", targetPath.toAbsolutePath());
            return relativePath;
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            logger.error("图片处理或保存失败", e);
            throw new BusinessException("图片上传失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String category) {
        validateFile(file);
        ensureOssReady();
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            String ext = getFileExtension(file.getOriginalFilename());

            if (useOss()) {
                String objectKey = buildObjectKey(category, fileName);
                uploadToOss(objectKey, file.getBytes(), contentTypeOf(ext));
                logger.info("文件已上传到 OSS: {}", objectKey);
                return objectKey;
            }

            Path categoryDir = Paths.get(uploadRootPath, category);
            Files.createDirectories(categoryDir);
            Path targetPath = categoryDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            String relativePath = category + "/" + fileName;
            logger.info("文件已保存到本地: {}", targetPath.toAbsolutePath());
            return relativePath;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("文件保存失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    private void uploadToOss(String objectKey, byte[] bytes, String contentType) {
        OSS oss = requireOss();
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(bytes.length);
        if (contentType != null) {
            meta.setContentType(contentType);
        }
        PutObjectRequest request = new PutObjectRequest(
                ossBucket, objectKey, new ByteArrayInputStream(bytes), meta);
        oss.putObject(request);
    }

    @Override
    public boolean deleteFile(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return false;
        }
        try {
            if (useOss()) {
                requireOss().deleteObject(ossBucket, objectName);
                return true;
            }
            Path filePath = Paths.get(uploadRootPath, objectName);
            return Files.deleteIfExists(filePath);
        } catch (Exception e) {
            logger.error("文件删除失败: {}", objectName, e);
            return false;
        }
    }

    @Override
    public FileUploadService.FileInfo getFileInfo(String objectName) {
        try {
            if (useOss()) {
                FileUploadService.FileInfo fileInfo = new FileUploadService.FileInfo();
                fileInfo.setFilePath(getFileUrl(objectName));
                fileInfo.setFileName(objectName.substring(objectName.lastIndexOf('/') + 1));
                fileInfo.setLastModified(LocalDateTime.now());
                return fileInfo;
            }

            Path filePath = Paths.get(uploadRootPath, objectName);
            if (!Files.exists(filePath)) {
                return null;
            }
            FileUploadService.FileInfo fileInfo = new FileUploadService.FileInfo();
            fileInfo.setFilePath(getFileUrl(objectName));
            fileInfo.setFileSize(Files.size(filePath));
            fileInfo.setLastModified(LocalDateTime.now());
            fileInfo.setFileName(filePath.getFileName().toString());
            if (isImageFile(objectName)) {
                try (InputStream is = Files.newInputStream(filePath)) {
                    BufferedImage image = ImageIO.read(is);
                    if (image != null) {
                        fileInfo.setWidth(image.getWidth());
                        fileInfo.setHeight(image.getHeight());
                    }
                }
            }
            return fileInfo;
        } catch (Exception e) {
            logger.error("获取文件信息失败: {}", objectName, e);
            return null;
        }
    }

    @Override
    public String getFileUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return null;
        }
        // 已是完整 URL
        if (objectName.startsWith("http://") || objectName.startsWith("https://")) {
            return objectName;
        }
        if (useOss()) {
            if (ossSignedUrl) {
                Date expiration = new Date(System.currentTimeMillis() + ossSignedExpireSeconds * 1000);
                return requireOss().generatePresignedUrl(ossBucket, objectName, expiration).toString();
            }
            if (ossUrlPrefix != null && !ossUrlPrefix.isBlank()) {
                String base = ossUrlPrefix.endsWith("/") ? ossUrlPrefix.substring(0, ossUrlPrefix.length() - 1) : ossUrlPrefix;
                return base + "/" + objectName;
            }
            String host = ossEndpoint == null ? "" : ossEndpoint
                    .replace("https://", "")
                    .replace("http://", "");
            return "https://" + ossBucket + "." + host + "/" + objectName;
        }
        return "/api/v1/files/view/" + objectName;
    }

    @Override
    public Path getAbsolutePath(String relativePath) {
        return Paths.get(uploadRootPath, relativePath).toAbsolutePath().normalize();
    }

    private void ensureOssReady() {
        if ("oss".equalsIgnoreCase(storageType) && ossClientProvider.getIfAvailable() == null) {
            throw new BusinessException(
                    "当前配置为阿里云 OSS，但未设置环境变量 ALIBABA_AIR_TEST1_ACCESSKEY_ID / ALIBABA_AIR_TEST1_ACCESSKEY_SECRET，或改回 FILE_STORAGE_TYPE=local");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        if (file.getSize() > maxFileSize) {
            throw new BusinessException("文件大小超过限制: " + (maxFileSize / 1024 / 1024) + "MB");
        }
    }

    private void validateImageFile(MultipartFile file) {
        String fileExtension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_IMAGE_TYPES.contains(fileExtension.toLowerCase())) {
            throw new BusinessException("不支持的图片格式，支持的格式: " + String.join(", ", ALLOWED_IMAGE_TYPES));
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            if (image == null) {
                throw new BusinessException("文件不是有效的图片格式");
            }
        } catch (IOException e) {
            throw new BusinessException("读取图片文件失败: " + e.getMessage());
        }
    }

    private BufferedImage processImage(byte[] fileBytes) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(fileBytes));
        if (originalImage == null) {
            throw new IOException("无法读取图片文件");
        }
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        if (originalWidth <= MAX_IMAGE_WIDTH && originalHeight <= MAX_IMAGE_HEIGHT) {
            return originalImage;
        }
        double scale = Math.min(
                (double) MAX_IMAGE_WIDTH / originalWidth,
                (double) MAX_IMAGE_HEIGHT / originalHeight
        );
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        logger.info("图片已压缩: {}x{} -> {}x{}", originalWidth, originalHeight, newWidth, newHeight);
        return scaledImage;
    }

    private String generateFileName(String originalFilename) {
        String fileExtension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + "." + fileExtension;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private boolean isImageFile(String filePath) {
        return ALLOWED_IMAGE_TYPES.contains(getFileExtension(filePath));
    }

    private String contentTypeOf(String extension) {
        return switch (extension == null ? "" : extension.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "bmp" -> "image/bmp";
            case "mp4" -> "video/mp4";
            case "pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }

    private byte[] compressImageOptimized(BufferedImage processedImage, byte[] originalBytes, String extension) throws IOException {
        try {
            byte[] compressedBytes = com.example.campussystem.util.ImageCompressor.compressImageSimple(
                    originalBytes, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
            logger.info("图片压缩完成: 原始={}KB, 压缩后={}KB",
                    originalBytes.length / 1024, compressedBytes.length / 1024);
            return compressedBytes;
        } catch (Exception e) {
            logger.warn("图片压缩失败，使用原始图片: {}", e.getMessage());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(processedImage, extension, baos);
            return baos.toByteArray();
        }
    }
}
