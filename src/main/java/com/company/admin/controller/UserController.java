package com.company.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.company.admin.annotation.OpLog;
import com.company.admin.common.PageResult;
import com.company.admin.common.Result;
import com.company.admin.dto.request.*;
import com.company.admin.entity.User;
import com.company.admin.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@Tag(name = "用户管理")
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/list")
    @Operation(summary = "用户分页列表")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result<PageResult<User>> list(UserQueryRequest request) {
        return Result.success(userService.page(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "用户详情")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result<User> getById(@Parameter(description = "主键 ID") @PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @OpLog(value = "新增用户", type = OpLog.LogType.INSERT)
    @PostMapping
    @Operation(summary = "新增用户")
    @PreAuthorize("hasAuthority('sys:user:add')")
    public Result<Void> create(@Valid @RequestBody UserCreateRequest request) {
        userService.create(request);
        return Result.success();
    }

    @OpLog(value = "修改用户", type = OpLog.LogType.UPDATE)
    @PutMapping
    @Operation(summary = "修改用户")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    public Result<Void> update(@Valid @RequestBody UserUpdateRequest request) {
        userService.update(request);
        return Result.success();
    }

    @OpLog(value = "删除用户", type = OpLog.LogType.DELETE)
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @PreAuthorize("hasAuthority('sys:user:delete')")
    public Result<Void> delete(@Parameter(description = "主键 ID") @PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}/roles")
    @Operation(summary = "获取用户角色 ID 列表")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result<?> getUserRoles(@Parameter(description = "主键 ID") @PathVariable Long id) {
        return Result.success(userService.getUserRoleIds(id));
    }

    @OpLog(value = "分配用户角色", type = OpLog.LogType.UPDATE)
    @PutMapping("/{id}/roles")
    @Operation(summary = "分配用户角色")
    @PreAuthorize("hasAuthority('sys:user:role')")
    public Result<Void> assignRoles(@Parameter(description = "主键 ID") @PathVariable Long id,
                                    @Valid @RequestBody UserRoleRequest request) {
        request.setUserId(id);
        userService.assignRoles(request);
        return Result.success();
    }

    @OpLog(value = "重置密码", type = OpLog.LogType.UPDATE)
    @PutMapping("/{id}/reset-password")
    @Operation(summary = "重置用户密码")
    @PreAuthorize("hasAuthority('sys:user:resetPwd')")
    public Result<Void> resetPassword(@Parameter(description = "主键 ID") @PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }

    @PutMapping("/profile")
    @Operation(summary = "修改个人资料")
    public Result<Void> updateProfile(Authentication authentication,
                                      @Valid @RequestBody UserUpdateRequest request) {
        Long userId = getCurrentUserId(authentication);
        userService.updateProfile(userId, request);
        return Result.success();
    }

    @PutMapping("/password")
    @Operation(summary = "修改当前用户密码")
    public Result<Void> updatePassword(Authentication authentication,
                                       @RequestBody PasswordUpdateRequest request) {
        Long userId = getCurrentUserId(authentication);
        userService.updatePassword(userId, request.getOldPassword(),
                request.getNewPassword());
        return Result.success();
    }

    private Long getCurrentUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.getByUsername(username);
        return user.getId();
    }
}
