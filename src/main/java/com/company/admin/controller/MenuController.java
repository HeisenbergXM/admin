package com.company.admin.controller;

import com.company.admin.annotation.OpLog;
import com.company.admin.common.Result;
import com.company.admin.dto.request.MenuCreateRequest;
import com.company.admin.entity.Menu;
import com.company.admin.service.MenuService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:menu:list')")
    public Result<?> list() {
        return Result.success(menuService.getAllMenus());
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('sys:menu:list')")
    public Result<?> tree() {
        return Result.success(menuService.getMenuTree());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:menu:list')")
    public Result<Menu> getById(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    @OpLog(value = "新增菜单", type = OpLog.LogType.INSERT)
    @PostMapping
    @PreAuthorize("hasAuthority('sys:menu:add')")
    public Result<Void> create(@Valid @RequestBody MenuCreateRequest request) {
        menuService.create(request);
        return Result.success();
    }

    @OpLog(value = "修改菜单", type = OpLog.LogType.UPDATE)
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:menu:edit')")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody MenuCreateRequest request) {
        menuService.update(id, request);
        return Result.success();
    }

    @OpLog(value = "删除菜单", type = OpLog.LogType.DELETE)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:menu:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }
}
