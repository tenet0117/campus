package com.example.campussystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI聊天响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIChatResponse {
    
    /**
     * AI回复内容
     */
    private String reply;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 错误信息（如果有）
     */
    private String error;
    
    /**
     * 使用的tokens数量
     */
    private Integer tokensUsed;
    
    // 手动添加 Builder 模式
    public static AIChatResponseBuilder builder() {
        return new AIChatResponseBuilder();
    }
    
    public static class AIChatResponseBuilder {
        private String reply;
        private String sessionId;
        private Long timestamp;
        private Boolean success;
        private String error;
        private Integer tokensUsed;
        
        public AIChatResponseBuilder reply(String reply) {
            this.reply = reply;
            return this;
        }
        
        public AIChatResponseBuilder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        public AIChatResponseBuilder timestamp(Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public AIChatResponseBuilder success(Boolean success) {
            this.success = success;
            return this;
        }
        
        public AIChatResponseBuilder error(String error) {
            this.error = error;
            return this;
        }
        
        public AIChatResponseBuilder tokensUsed(Integer tokensUsed) {
            this.tokensUsed = tokensUsed;
            return this;
        }
        
        public AIChatResponse build() {
            AIChatResponse response = new AIChatResponse();
            response.reply = this.reply;
            response.sessionId = this.sessionId;
            response.timestamp = this.timestamp;
            response.success = this.success;
            response.error = this.error;
            response.tokensUsed = this.tokensUsed;
            return response;
        }
    }
}
