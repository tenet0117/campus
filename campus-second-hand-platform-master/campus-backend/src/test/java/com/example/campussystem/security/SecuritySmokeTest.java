package com.example.campussystem.security;

import com.example.campussystem.config.SecurityConfig;
import com.example.campussystem.controller.HealthController;
import com.example.campussystem.controller.ProductController;
import com.example.campussystem.service.ProductService;
import com.example.campussystem.util.JwtUtil;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 安全相关冒烟：公开接口可访问、受保护写接口无 token 返回 401
 */
@SpringBootTest(classes = SecuritySmokeTest.TestApp.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "jwt.secret=campus-test-jwt-secret-key-32chars-min",
        "jwt.expiration=86400000",
        "jwt.header=Authorization",
        "jwt.prefix=Bearer ",
        "app.cors.allowed-origins=http://localhost:5173",
        "app.cors.allowed-methods=GET,POST,PUT,PATCH,DELETE,OPTIONS",
        "app.cors.allowed-headers=*",
        "app.cors.allow-credentials=true",
        "app.swagger.enabled=false",
        "spring.main.banner-mode=off"
})
class SecuritySmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private ProductService productService;

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            RedisAutoConfiguration.class,
            MybatisPlusAutoConfiguration.class
    })
    @EnableWebSecurity
    @Import({
            SecurityConfig.class,
            HealthController.class,
            ProductController.class,
            JwtAuthenticationEntryPoint.class,
            JwtAccessDeniedHandler.class,
            JwtAuthenticationFilter.class
    })
    static class TestApp {
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }

    @Test
    void postProductWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"test\",\"price\":1,\"categoryId\":1}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }
}
