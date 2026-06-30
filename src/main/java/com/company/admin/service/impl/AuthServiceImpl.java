package com.company.admin.service.impl;

import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.dto.request.LoginRequest;
import com.company.admin.dto.response.LoginResponse;
import com.company.admin.dto.response.MenuTreeResponse;
import com.company.admin.dto.response.UserInfoResponse;
import com.company.admin.entity.LoginLog;
import com.company.admin.entity.Menu;
import com.company.admin.entity.User;
import com.company.admin.mapper.LoginLogMapper;
import com.company.admin.mapper.MenuMapper;
import com.company.admin.mapper.UserMapper;
import com.company.admin.security.JwtTokenUtil;
import com.company.admin.service.AuthService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserMapper userMapper;
    private final MenuMapper menuMapper;
    private final LoginLogMapper loginLogMapper;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtTokenUtil jwtTokenUtil,
                           UserMapper userMapper,
                           MenuMapper menuMapper,
                           LoginLogMapper loginLogMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userMapper = userMapper;
        this.menuMapper = menuMapper;
        this.loginLogMapper = loginLogMapper;
    }

    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword()));
            String token = jwtTokenUtil.generateToken(authentication.getName());
            User user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getUsername, request.getUsername()));

            // 记录登录成功日志
            LoginLog loginLog = new LoginLog();
            loginLog.setUsername(request.getUsername());
            loginLog.setLoginType(1);
            loginLog.setIp(getIp(httpRequest));
            loginLog.setStatus(1);
            loginLog.setMessage("登录成功");
            loginLog.setCreateTime(LocalDateTime.now());
            loginLogMapper.insert(loginLog);

            return new LoginResponse(token, user.getId());
        } catch (BadCredentialsException e) {
            recordLoginFail(request.getUsername(), httpRequest, "用户名或密码错误");
            throw new BusinessException(ErrorCode.USERNAME_PASSWORD_ERROR);
        } catch (DisabledException e) {
            recordLoginFail(request.getUsername(), httpRequest, "用户已禁用");
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
    }

    @Override
    public UserInfoResponse getCurrentUserInfo(String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }

        // 获取用户角色编码
        List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());

        // 获取用户权限
        Set<String> permissions = new HashSet<>(userMapper.selectPermissionsByUserId(user.getId()));

        // 获取用户菜单
        List<Menu> menus = menuMapper.selectByUserId(user.getId());

        UserInfoResponse response = new UserInfoResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setRoles(roles);
        response.setPermissions(permissions);
        response.setMenus(buildMenuTree(menus));
        return response;
    }

    private List<MenuTreeResponse> buildMenuTree(List<Menu> menus) {
        List<Menu> navMenus = menus.stream()
                .filter(menu -> menu.getMenuType() == null || menu.getMenuType() != 3)
                .collect(Collectors.toList());
        return buildMenuTree(navMenus, 0L);
    }

    private List<MenuTreeResponse> buildMenuTree(List<Menu> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> parentId.equals(menu.getParentId()))
                .map(menu -> {
                    MenuTreeResponse node = new MenuTreeResponse();
                    BeanUtils.copyProperties(menu, node);
                    node.setChildren(buildMenuTree(menus, menu.getId()));
                    return node;
                })
                .collect(Collectors.toList());
    }

    private void recordLoginFail(String username, HttpServletRequest request,
                                  String message) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUsername(username);
        loginLog.setLoginType(1);
        loginLog.setIp(getIp(request));
        loginLog.setStatus(0);
        loginLog.setMessage(message);
        loginLog.setCreateTime(LocalDateTime.now());
        loginLogMapper.insert(loginLog);
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
