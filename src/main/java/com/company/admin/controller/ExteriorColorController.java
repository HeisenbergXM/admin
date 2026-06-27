package com.company.admin.controller;

import com.company.admin.annotation.OpLog;
import com.company.admin.common.PageResult;
import com.company.admin.common.Result;
import com.company.admin.dto.request.ExteriorColorCreateRequest;
import com.company.admin.dto.request.ExteriorColorQueryRequest;
import com.company.admin.dto.request.ExteriorColorUpdateRequest;
import com.company.admin.entity.ExteriorColor;
import com.company.admin.service.ExteriorColorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 外饰颜色控制器
 */
@RestController
@RequestMapping("/api/master-data/exterior-colors")
@Tag(name = "外饰颜色")
public class ExteriorColorController {

    private final ExteriorColorService exteriorColorService;

    public ExteriorColorController(ExteriorColorService exteriorColorService) {
        this.exteriorColorService = exteriorColorService;
    }

    @GetMapping
    @Operation(summary = "外饰颜色分页列表")
    @PreAuthorize("hasAuthority('sys:master:list')")
    public Result<PageResult<ExteriorColor>> listExteriorColors(ExteriorColorQueryRequest request) {
        return Result.success(exteriorColorService.pageExteriorColors(request));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有启用外饰颜色（下拉用）")
    public Result<List<ExteriorColor>> listAllEnabled() {
        return Result.success(exteriorColorService.listAllEnabled());
    }

    @PostMapping
    @Operation(summary = "新增外饰颜色")
    @PreAuthorize("hasAuthority('sys:master:add')")
    @OpLog(value = "新增外饰颜色", type = OpLog.LogType.INSERT)
    public Result<Void> createExteriorColor(@Valid @RequestBody ExteriorColorCreateRequest request) {
        exteriorColorService.createExteriorColor(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑外饰颜色")
    @PreAuthorize("hasAuthority('sys:master:edit')")
    @OpLog(value = "编辑外饰颜色", type = OpLog.LogType.UPDATE)
    public Result<Void> updateExteriorColor(@PathVariable Long id,
                                            @Valid @RequestBody ExteriorColorUpdateRequest request) {
        request.setId(id);
        exteriorColorService.updateExteriorColor(request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除外饰颜色")
    @PreAuthorize("hasAuthority('sys:master:delete')")
    @OpLog(value = "删除外饰颜色", type = OpLog.LogType.DELETE)
    public Result<Void> deleteExteriorColor(@PathVariable Long id) {
        exteriorColorService.deleteExteriorColor(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "启停外饰颜色")
    @PreAuthorize("hasAuthority('sys:master:edit')")
    @OpLog(value = "启停外饰颜色", type = OpLog.LogType.UPDATE)
    public Result<Void> toggleExteriorColorStatus(@PathVariable Long id,
                                                  @RequestBody Map<String, Integer> body) {
        exteriorColorService.toggleExteriorColorStatus(id, body.get("status"));
        return Result.success();
    }
}