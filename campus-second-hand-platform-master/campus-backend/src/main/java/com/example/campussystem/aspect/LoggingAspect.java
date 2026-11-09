package com.example.campussystem.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * 日志切面
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 定义切点：所有Controller方法
     */
    @Pointcut("execution(* com.example.campussystem.controller..*(..))")
    public void controllerMethods() {}

    /**
     * 定义切点：所有Service方法
     */
    @Pointcut("execution(* com.example.campussystem.service..*(..))")
    public void serviceMethods() {}

    /**
     * Controller方法执行前后的日志记录
     */
    @Around("controllerMethods()")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
        }
        
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        // 记录请求开始日志
        if (request != null) {
            logger.info("==> {} {} - {}#{} - IP: {} - 参数: {}", 
                       request.getMethod(), 
                       request.getRequestURI(),
                       className, 
                       methodName,
                       getClientIpAddress(request),
                       formatArgs(args));
        } else {
            logger.info("==> {}#{} - 参数: {}", className, methodName, formatArgs(args));
        }
        
        Object result = null;
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            // 记录请求完成日志
            if (request != null) {
                logger.info("<== {} {} - {}#{} - 耗时: {}ms - 状态: 成功", 
                           request.getMethod(), 
                           request.getRequestURI(),
                           className, 
                           methodName,
                           executionTime);
            } else {
                logger.info("<== {}#{} - 耗时: {}ms - 状态: 成功", className, methodName, executionTime);
            }
            
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            // 记录异常日志
            if (request != null) {
                logger.error("<== {} {} - {}#{} - 耗时: {}ms - 状态: 异常 - 错误: {}", 
                            request.getMethod(), 
                            request.getRequestURI(),
                            className, 
                            methodName,
                            executionTime,
                            e.getMessage());
            } else {
                logger.error("<== {}#{} - 耗时: {}ms - 状态: 异常 - 错误: {}", 
                            className, methodName, executionTime, e.getMessage());
            }
            
            throw e;
        }
    }

    /**
     * Service方法执行前的日志记录
     */
    @Before("serviceMethods()")
    public void logServiceMethodEntry(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        logger.debug("进入服务方法: {}#{} - 参数: {}", className, methodName, formatArgs(args));
    }

    /**
     * Service方法正常返回后的日志记录
     */
    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logServiceMethodReturn(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        logger.debug("退出服务方法: {}#{} - 返回值类型: {}", 
                    className, methodName, 
                    result != null ? result.getClass().getSimpleName() : "null");
    }

    /**
     * Service方法抛出异常后的日志记录
     */
    @AfterThrowing(pointcut = "serviceMethods()", throwing = "exception")
    public void logServiceMethodException(JoinPoint joinPoint, Throwable exception) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        logger.error("服务方法异常: {}#{} - 异常: {}", className, methodName, exception.getMessage(), exception);
    }

    /**
     * 格式化方法参数
     */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        
        try {
            // 过滤敏感信息
            Object[] filteredArgs = Arrays.stream(args)
                    .map(this::filterSensitiveData)
                    .toArray();
            
            return objectMapper.writeValueAsString(filteredArgs);
        } catch (Exception e) {
            return Arrays.toString(args);
        }
    }

    /**
     * 过滤敏感数据
     */
    private Object filterSensitiveData(Object arg) {
        if (arg == null) {
            return null;
        }
        
        String argString = arg.toString();
        
        // 过滤密码字段
        if (argString.contains("password")) {
            return "***FILTERED***";
        }
        
        // 过滤token字段
        if (argString.contains("token") || argString.contains("Token")) {
            return "***FILTERED***";
        }
        
        // 过滤HttpServletRequest等不需要记录的对象
        if (arg instanceof HttpServletRequest) {
            return "HttpServletRequest";
        }
        
        return arg;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}