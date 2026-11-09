package com.example.campussystem.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 客户端（storage-type=oss 且已配置 AccessKey 时启用）
 */
@Configuration
public class OssConfig {

    private static final Logger logger = LoggerFactory.getLogger(OssConfig.class);

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(name = "file.storage-type", havingValue = "oss")
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${file.oss.access-key-id:}')")
    public OSS ossClient(
            @Value("${file.oss.endpoint}") String endpoint,
            @Value("${file.oss.access-key-id}") String accessKeyId,
            @Value("${file.oss.access-key-secret}") String accessKeySecret) {
        String ep = endpoint.startsWith("http") ? endpoint : "https://" + endpoint;
        logger.info("初始化阿里云 OSS 客户端, endpoint={}", ep);
        return new OSSClientBuilder().build(ep, accessKeyId, accessKeySecret);
    }
}
