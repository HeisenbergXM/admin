package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.dto.request.*;
import com.company.admin.entity.User;
import java.util.List;

public interface UserService {
    PageResult<User> page(UserQueryRequest request);
    User getById(Long id);
    User getByUsername(String username);
    void create(UserCreateRequest request);
    void update(UserUpdateRequest request);
    void delete(Long id);
    List<Long> getUserRoleIds(Long userId);
    void assignRoles(UserRoleRequest request);
    void resetPassword(Long id);
    void updateProfile(Long userId, UserUpdateRequest request);
    void updatePassword(Long userId, String oldPassword, String newPassword);
}
