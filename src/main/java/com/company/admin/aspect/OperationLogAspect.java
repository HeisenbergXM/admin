package com.company.admin.aspect;

import com.company.admin.annotation.OpLog;
import com.company.admin.mapper.OperationLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OperationLogAspect(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Around("@annotation(com.company.admin.annotation.OpLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            saveLog(joinPoint, result, exception, elapsed);
        }
    }

    @Async
    public void saveLog(ProceedingJoinPoint joinPoint, Object result,
                        Exception exception, long elapsed) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OpLog annotation = method.getAnnotation(OpLog.class);

        com.company.admin.entity.OperationLog entity = new com.company.admin.entity.OperationLog();
        entity.setOperation(annotation.value());
        entity.setMethod(method.getDeclaringClass().getSimpleName() + "." + method.getName());
        entity.setCreateTime(LocalDateTime.now());

        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            entity.setUsername(authentication.getName());
        }

        // 获取 IP
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            entity.setIp(getIp(request));
        }

        // 保存请求参数
        if (annotation.saveParams()) {
            try {
                entity.setParams(objectMapper.writeValueAsString(joinPoint.getArgs()));
            } catch (Exception ignored) {}
        }

        // 保存返回结果
        if (annotation.saveResult() && exception == null) {
            try {
                entity.setResult(objectMapper.writeValueAsString(result));
            } catch (Exception ignored) {}
        }

        // 记录异常
        if (exception != null) {
            entity.setResult("异常: " + exception.getMessage());
        }

        operationLogMapper.insert(entity);
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
