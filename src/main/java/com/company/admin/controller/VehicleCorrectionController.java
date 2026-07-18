package com.company.admin.controller;

import com.company.admin.annotation.OpLog;
import com.company.admin.common.PageResult;
import com.company.admin.common.Result;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest;
import com.company.admin.dto.request.VehicleQueryRequest;
import com.company.admin.dto.response.VehicleCorrectionListResponse;
import com.company.admin.dto.response.VehiclePanoramaResponse;
import com.company.admin.service.VehicleCorrectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/vehicle-corrections")
@RequiredArgsConstructor
@Tag(name = "车辆数据修订")
public class VehicleCorrectionController {

    private final VehicleCorrectionService vehicleCorrectionService;

    @GetMapping
    @PreAuthorize("hasAuthority('vlm:vehicle-correction:list')")
    public Result<PageResult<VehicleCorrectionListResponse>> list(VehicleQueryRequest request) {
        return Result.success(vehicleCorrectionService.pageCorrections(request));
    }

    @GetMapping("/{vehicleId}")
    @PreAuthorize("hasAuthority('vlm:vehicle-correction:list')")
    public Result<VehiclePanoramaResponse> detail(@PathVariable Long vehicleId) {
        return Result.success(vehicleCorrectionService.getCorrection(vehicleId));
    }

    @PutMapping("/{vehicleId}")
    @PreAuthorize("hasAuthority('vlm:vehicle-correction:edit')")
    @OpLog(value = "车辆数据修订", type = OpLog.LogType.UPDATE)
    public Result<Void> update(@PathVariable Long vehicleId,
                               @Valid @RequestBody VehicleCorrectionUpdateRequest request) {
        vehicleCorrectionService.updateCorrection(vehicleId, request);
        return Result.success();
    }
}
