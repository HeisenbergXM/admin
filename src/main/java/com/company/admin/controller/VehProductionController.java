package com.company.admin.controller;

import com.company.admin.annotation.OpLog;
import com.company.admin.common.PageResult;
import com.company.admin.common.Result;
import com.company.admin.dto.request.ProductionQueryRequest;
import com.company.admin.dto.request.ProductionSaveRequest;
import com.company.admin.dto.response.ProductionResponse;
import com.company.admin.dto.response.VehicleListResponse;
import com.company.admin.service.VehProductionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/production")
@RequiredArgsConstructor
@Tag(name = "生产录入")
public class VehProductionController {

    private final VehProductionService vehProductionService;

    @GetMapping
    @Operation(summary = "生产录入待办列表")
    @PreAuthorize("hasAuthority('sys:vehicle:list')")
    public Result<PageResult<VehicleListResponse>> listProductions(ProductionQueryRequest request) {
        return Result.success(vehProductionService.pageProductions(request));
    }

    @PostMapping
    @Operation(summary = "创建车辆和生产草稿")
    @PreAuthorize("hasAuthority('sys:vehicle:add')")
    @OpLog(value = "创建车辆生产草稿", type = OpLog.LogType.INSERT)
    public Result<Long> createProduction(@Valid @RequestBody ProductionSaveRequest request) {
        return Result.success(vehProductionService.createProduction(request));
    }

    @PutMapping("/{vehicleId}")
    @Operation(summary = "更新生产草稿")
    @PreAuthorize("hasAuthority('sys:vehicle:edit')")
    @OpLog(value = "更新生产草稿", type = OpLog.LogType.UPDATE)
    public Result<Void> updateProduction(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId,
                                         @Valid @RequestBody ProductionSaveRequest request) {
        vehProductionService.updateProduction(vehicleId, request);
        return Result.success();
    }

    @PostMapping("/{vehicleId}/confirm")
    @Operation(summary = "确认生产录入")
    @PreAuthorize("hasAuthority('sys:vehicle:confirm')")
    @OpLog(value = "确认生产录入", type = OpLog.LogType.UPDATE)
    public Result<Void> confirmProduction(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId) {
        vehProductionService.confirmProduction(vehicleId);
        return Result.success();
    }

    @GetMapping("/{vehicleId}")
    @Operation(summary = "获取生产数据")
    @PreAuthorize("hasAuthority('sys:vehicle:list')")
    public Result<ProductionResponse> getProduction(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId) {
        return Result.success(vehProductionService.getProduction(vehicleId));
    }
}
