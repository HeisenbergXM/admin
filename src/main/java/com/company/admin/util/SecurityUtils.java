package com.company.admin.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具类 —— 从 Spring Security 获取当前登录用户信息
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 获取当前登录用户名，未登录返回 null
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String name = authentication.getName();
            // Spring Security 匿名用户的 name 为 "anonymousUser"
            if ("anonymousUser".equals(name)) {
                return null;
            }
            return name;
        }
        return null;
    }
}
