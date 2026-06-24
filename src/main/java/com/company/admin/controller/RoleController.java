package com.company.admin.controller;

import com.company.admin.annotation.OpLog;
import com.company.admin.common.Result;
import com.company.admin.dto.request.RoleCreateRequest;
import com.company.admin.dto.request.RoleMenuRequest;
import com.company.admin.entity.Role;
import com.company.admin.service.RoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:role:list')")
    public Result<?> list() {
        return Result.success(roleService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:list')")
    public Result<Role> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @OpLog(value = "新增角色", type = OpLog.LogType.INSERT)
    @PostMapping
    @PreAuthorize("hasAuthority('sys:role:add')")
    public Result<Void> create(@Valid @RequestBody RoleCreateRequest request) {
        roleService.create(request);
        return Result.success();
    }

    @OpLog(value = "修改角色", type = OpLog.LogType.UPDATE)
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:edit')")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody RoleCreateRequest request) {
        roleService.update(id, request);
        return Result.success();
    }

    @OpLog(value = "删除角色", type = OpLog.LogType.DELETE)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}/menus")
    @PreAuthorize("hasAuthority('sys:role:list')")
    public Result<?> getRoleMenus(@PathVariable Long id) {
        return Result.success(roleService.getRoleMenuIds(id));
    }

    @OpLog(value = "分配角色菜单", type = OpLog.LogType.UPDATE)
    @PutMapping("/{id}/menus")
    @PreAuthorize("hasAuthority('sys:role:menu')")
    public Result<Void> assignMenus(@PathVariable Long id,
                                    @Valid @RequestBody RoleMenuRequest request) {
        request.setRoleId(id);
        roleService.assignMenus(request);
        return Result.success();
    }
}
