package com.company.admin.security;

import com.company.admin.entity.LoginLog;
import com.company.admin.mapper.LoginLogMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Component
public class CustomLogoutHandler implements LogoutHandler {

    private final LoginLogMapper loginLogMapper;

    public CustomLogoutHandler(LoginLogMapper loginLogMapper) {
        this.loginLogMapper = loginLogMapper;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
                       Authentication authentication) {
        if (authentication != null) {
            LoginLog log = new LoginLog();
            log.setUsername(authentication.getName());
            log.setLoginType(2);
            log.setIp(getIp(request));
            log.setStatus(1);
            log.setMessage("登出成功");
            log.setCreateTime(LocalDateTime.now());
            loginLogMapper.insert(log);
        }
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
