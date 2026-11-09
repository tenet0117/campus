package com.example.campussystem.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

/**
 * 图片压缩工具类 - 简化版本，兼容性更好
 */
public class ImageCompressor {
    
    private static final int DEFAULT_MAX_WIDTH = 1920;
    private static final int DEFAULT_MAX_HEIGHT = 1080;
    private static final float DEFAULT_JPEG_QUALITY = 0.85f;
    
    /**
     * 压缩图片
     * @param imageBytes 原始图片字节数组
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @param quality 压缩质量 (0.0-1.0) - 仅对JPEG有效
     * @param format 输出格式 (jpg, png, gif)
     * @return 压缩后的图片字节数组
     */
    public static byte[] compressImage(byte[] imageBytes, int maxWidth, int maxHeight, float quality, String format) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
        BufferedImage originalImage = ImageIO.read(bais);
        
        if (originalImage == null) {
            throw new IOException("无法读取图片文件");
        }
        
        // 调整尺寸
        BufferedImage resizedImage = resizeImage(originalImage, maxWidth, maxHeight);
        
        // 压缩并转换格式
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // 标准化格式名称
        String outputFormat = format.toLowerCase();
        if (outputFormat.equals("jpeg")) {
            outputFormat = "jpg";
        }
        
        // 对于JPEG，使用更简单的压缩方法
        if ("jpg".equals(outputFormat)) {
            // 转换为RGB色彩空间以确保JPEG兼容性
            BufferedImage rgbImage = convertToRgb(resizedImage);
            boolean success = ImageIO.write(rgbImage, "jpg", baos);
            if (!success) {
                throw new IOException("JPEG写入失败");
            }
        } else {
            // 其他格式使用标准方式
            boolean success = ImageIO.write(resizedImage, outputFormat, baos);
            if (!success) {
                throw new IOException("图片写入失败，格式: " + outputFormat);
            }
        }
        
        return baos.toByteArray();
    }
    
    /**
     * 简单的图片压缩方法（推荐使用）
     * @param imageBytes 原始图片字节数组
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 压缩后的图片字节数组
     */
    public static byte[] compressImageSimple(byte[] imageBytes, int maxWidth, int maxHeight) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
        BufferedImage originalImage = ImageIO.read(bais);
        
        if (originalImage == null) {
            throw new IOException("无法读取图片文件");
        }
        
        // 调整尺寸
        BufferedImage resizedImage = resizeImage(originalImage, maxWidth, maxHeight);
        
        // 自动选择最佳输出格式
        String format = getOptimalFormat(imageBytes, resizedImage);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean success = ImageIO.write(resizedImage, format, baos);
        
        if (!success) {
            throw new IOException("图片压缩失败");
        }
        
        return baos.toByteArray();
    }
    
    /**
     * 调整图片尺寸
     */
    private static BufferedImage resizeImage(BufferedImage originalImage, int maxWidth, int maxHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // 如果图片尺寸在限制范围内，直接返回
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return originalImage;
        }
        
        // 计算缩放比例
        double scaleWidth = (double) maxWidth / originalWidth;
        double scaleHeight = (double) maxHeight / originalHeight;
        double scale = Math.min(scaleWidth, scaleHeight);
        
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);
        
        // 创建缩放后的图片
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        
        // 设置高质量渲染
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        return resizedImage;
    }
    
    /**
     * 转换为RGB色彩空间
     */
    private static BufferedImage convertToRgb(BufferedImage image) {
        BufferedImage rgbImage = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = rgbImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return rgbImage;
    }
    
    /**
     * 获取最佳输出格式
     */
    private static String getOptimalFormat(byte[] originalBytes, BufferedImage image) {
        String originalFormat = getImageFormat(originalBytes);
        
        // 如果原图是PNG/BMP/WEBP但很大，转换为JPEG以减小文件大小
        if (("png".equals(originalFormat) || "bmp".equals(originalFormat) || "webp".equals(originalFormat)) && 
            (image.getWidth() > 1200 || image.getHeight() > 1200)) {
            return "jpg";
        }
        
        // 其他情况保持原格式，除非不支持
        if ("jpg".equals(originalFormat) || "jpeg".equals(originalFormat)) {
            return "jpg";
        } else if ("png".equals(originalFormat)) {
            return "png";
        } else if ("gif".equals(originalFormat)) {
            return "gif";
        } else if ("webp".equals(originalFormat)) {
            return "webp";
        }
        
        // 默认使用JPEG
        return "jpg";
    }
    
    /**
     * 获取图片信息
     */
    public static ImageInfo getImageInfo(byte[] imageBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
        BufferedImage image = ImageIO.read(bais);
        
        if (image == null) {
            throw new IOException("无法读取图片文件");
        }
        
        ImageInfo info = new ImageInfo();
        info.setWidth(image.getWidth());
        info.setHeight(image.getHeight());
        info.setSize(imageBytes.length);
        info.setFormat(getImageFormat(imageBytes));
        
        return info;
    }
    
    /**
     * 获取图片格式
     */
    private static String getImageFormat(byte[] imageBytes) {
        if (imageBytes.length < 4) {
            return "unknown";
        }
        
        // 检查文件头
        if (imageBytes[0] == (byte)0xFF && imageBytes[1] == (byte)0xD8) {
            return "jpg";
        } else if (imageBytes[0] == (byte)0x89 && imageBytes[1] == 0x50 && 
                   imageBytes[2] == 0x4E && imageBytes[3] == 0x47) {
            return "png";
        } else if (imageBytes[0] == 0x47 && imageBytes[1] == 0x49 && 
                   imageBytes[2] == 0x46) {
            return "gif";
        } else if (imageBytes.length > 11 && 
                   new String(imageBytes, 0, 11).contains("WEBP")) {
            return "webp";
        }
        
        return "unknown";
    }
    
    /**
     * 图片信息类
     */
    public static class ImageInfo {
        private int width;
        private int height;
        private long size;
        private String format;
        
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
        
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
        
        @Override
        public String toString() {
            return String.format("ImageInfo{width=%d, height=%d, size=%dKB, format='%s'}", 
                               width, height, size / 1024, format);
        }
    }
}
