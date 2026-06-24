package com.company.admin.service;

import com.company.admin.dto.request.LoginRequest;
import com.company.admin.dto.response.LoginResponse;
import com.company.admin.dto.response.UserInfoResponse;
import javax.servlet.http.HttpServletRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);
    UserInfoResponse getCurrentUserInfo(String username);
}
