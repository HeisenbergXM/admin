package com.company.admin.controller;

import com.company.admin.common.PageResult;
import com.company.admin.common.Result;
import com.company.admin.dto.request.VehicleCandidateRequest;
import com.company.admin.dto.request.VehicleQueryRequest;
import com.company.admin.dto.response.VehicleBasicInfo;
import com.company.admin.dto.response.VehicleDetailResponse;
import com.company.admin.dto.response.VehicleListResponse;
import com.company.admin.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "车辆通用查询")
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    @Operation(summary = "车辆分页列表")
    @PreAuthorize("hasAuthority('sys:vehicle:list')")
    public Result<PageResult<VehicleListResponse>> listVehicles(VehicleQueryRequest request) {
        return Result.success(vehicleService.pageVehicles(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "车辆详情")
    @PreAuthorize("hasAuthority('sys:vehicle:list')")
    public Result<VehicleDetailResponse> getDetail(@Parameter(description = "主键 ID") @PathVariable Long id) {
        return Result.success(vehicleService.getDetail(id));
    }

    @GetMapping("/{id}/basic-info")
    @Operation(summary = "车辆基本信息")
    public Result<VehicleBasicInfo> getBasicInfo(@Parameter(description = "主键 ID") @PathVariable Long id) {
        return Result.success(vehicleService.getBasicInfo(id));
    }

    @GetMapping("/candidates")
    @Operation(summary = "VIN 候选列表")
    public Result<List<VehicleBasicInfo>> listCandidates(VehicleCandidateRequest request) {
        return Result.success(vehicleService.listCandidates(request));
    }

    @GetMapping("/check-vin")
    @Operation(summary = "VIN 唯一性校验")
    public Result<Boolean> checkVin(@Parameter(description = "车辆识别码 VIN") @RequestParam String vin) {
        return Result.success(vehicleService.checkVinAvailable(vin));
    }
}
