package com.company.admin.controller;

import com.company.admin.annotation.OpLog;
import com.company.admin.common.PageResult;
import com.company.admin.common.Result;
import com.company.admin.dto.request.ModelCreateRequest;
import com.company.admin.dto.request.ModelQueryRequest;
import com.company.admin.dto.request.ModelUpdateRequest;
import com.company.admin.entity.VehicleModel;
import com.company.admin.service.VehicleModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 车型数据控制器
 */
@RestController
@RequestMapping("/api/master-data/models")
@Tag(name = "车型数据")
public class VehicleModelController {

    private final VehicleModelService vehicleModelService;

    public VehicleModelController(VehicleModelService vehicleModelService) {
        this.vehicleModelService = vehicleModelService;
    }

    @GetMapping
    @Operation(summary = "车型分页列表")
    @PreAuthorize("hasAuthority('sys:master:list')")
    public Result<PageResult<VehicleModel>> listModels(ModelQueryRequest request) {
        return Result.success(vehicleModelService.pageModels(request));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有启用车型（下拉用）")
    public Result<List<VehicleModel>> listAllEnabled() {
        return Result.success(vehicleModelService.listAllEnabled());
    }

    @GetMapping("/series")
    @Operation(summary = "获取车系列表（去重，下拉用）")
    public Result<List<String>> listSeries() {
        return Result.success(vehicleModelService.listSeries());
    }

    @PostMapping
    @Operation(summary = "新增车型")
    @PreAuthorize("hasAuthority('sys:master:add')")
    @OpLog(value = "新增车型", type = OpLog.LogType.INSERT)
    public Result<Void> createModel(@Valid @RequestBody ModelCreateRequest request) {
        vehicleModelService.createModel(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑车型")
    @PreAuthorize("hasAuthority('sys:master:edit')")
    @OpLog(value = "编辑车型", type = OpLog.LogType.UPDATE)
    public Result<Void> updateModel(@PathVariable Long id,
                                    @Valid @RequestBody ModelUpdateRequest request) {
        request.setId(id);
        vehicleModelService.updateModel(request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除车型")
    @PreAuthorize("hasAuthority('sys:master:delete')")
    @OpLog(value = "删除车型", type = OpLog.LogType.DELETE)
    public Result<Void> deleteModel(@PathVariable Long id) {
        vehicleModelService.deleteModel(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "启停车型")
    @PreAuthorize("hasAuthority('sys:master:edit')")
    @OpLog(value = "启停车型", type = OpLog.LogType.UPDATE)
    public Result<Void> toggleModelStatus(@PathVariable Long id,
                                          @RequestBody Map<String, Integer> body) {
        vehicleModelService.toggleModelStatus(id, body.get("status"));
        return Result.success();
    }
}