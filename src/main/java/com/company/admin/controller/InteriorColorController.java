package com.company.admin.controller;

import com.company.admin.annotation.OpLog;
import com.company.admin.common.PageResult;
import com.company.admin.common.Result;
import com.company.admin.dto.request.InteriorColorCreateRequest;
import com.company.admin.dto.request.InteriorColorQueryRequest;
import com.company.admin.dto.request.InteriorColorUpdateRequest;
import com.company.admin.entity.InteriorColor;
import com.company.admin.service.InteriorColorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 内饰颜色控制器
 */
@RestController
@RequestMapping("/api/master-data/interior-colors")
@Tag(name = "内饰颜色")
public class InteriorColorController {

    private final InteriorColorService interiorColorService;

    public InteriorColorController(InteriorColorService interiorColorService) {
        this.interiorColorService = interiorColorService;
    }

    @GetMapping
    @Operation(summary = "内饰颜色分页列表")
    @PreAuthorize("hasAuthority('sys:master:list')")
    public Result<PageResult<InteriorColor>> listInteriorColors(InteriorColorQueryRequest request) {
        return Result.success(interiorColorService.pageInteriorColors(request));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有启用的内饰颜色（下拉用）")
    public Result<List<InteriorColor>> listAllEnabled() {
        return Result.success(interiorColorService.listAllEnabled());
    }

    @PostMapping
    @Operation(summary = "新增内饰颜色")
    @PreAuthorize("hasAuthority('sys:master:add')")
    @OpLog(value = "新增内饰颜色", type = OpLog.LogType.INSERT)
    public Result<Void> createInteriorColor(@Valid @RequestBody InteriorColorCreateRequest request) {
        interiorColorService.createInteriorColor(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑内饰颜色")
    @PreAuthorize("hasAuthority('sys:master:edit')")
    @OpLog(value = "编辑内饰颜色", type = OpLog.LogType.UPDATE)
    public Result<Void> updateInteriorColor(@PathVariable Long id,
                                            @Valid @RequestBody InteriorColorUpdateRequest request) {
        request.setId(id);
        interiorColorService.updateInteriorColor(request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除内饰颜色")
    @PreAuthorize("hasAuthority('sys:master:delete')")
    @OpLog(value = "删除内饰颜色", type = OpLog.LogType.DELETE)
    public Result<Void> deleteInteriorColor(@PathVariable Long id) {
        interiorColorService.deleteInteriorColor(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "启停内饰颜色")
    @PreAuthorize("hasAuthority('sys:master:edit')")
    @OpLog(value = "启停内饰颜色", type = OpLog.LogType.UPDATE)
    public Result<Void> toggleInteriorColorStatus(@PathVariable Long id,
                                                  @RequestBody Map<String, Integer> body) {
        interiorColorService.toggleInteriorColorStatus(id, body.get("status"));
        return Result.success();
    }
}