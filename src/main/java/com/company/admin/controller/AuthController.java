package com.company.admin.controller;

import com.company.admin.common.Result;
import com.company.admin.dto.request.LoginRequest;
import com.company.admin.dto.response.LoginResponse;
import com.company.admin.dto.response.UserInfoResponse;
import com.company.admin.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                        HttpServletRequest httpRequest) {
        LoginResponse response = authService.login(request, httpRequest);
        return Result.success("登录成功", response);
    }

    @GetMapping("/info")
    public Result<UserInfoResponse> info(Authentication authentication) {
        UserInfoResponse response = authService.getCurrentUserInfo(
                authentication.getName());
        return Result.success(response);
    }
}
