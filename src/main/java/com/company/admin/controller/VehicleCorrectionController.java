package com.company.admin.controller;

import com.company.admin.common.PageResult;
import com.company.admin.common.Result;
import com.company.admin.dto.request.VehicleQueryRequest;
import com.company.admin.dto.response.VehicleListResponse;
import com.company.admin.dto.response.VehiclePanoramaResponse;
import com.company.admin.service.VehicleCorrectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicle-corrections")
@RequiredArgsConstructor
@Tag(name = "车辆数据修订")
public class VehicleCorrectionController {

    private final VehicleCorrectionService vehicleCorrectionService;

    @GetMapping
    @PreAuthorize("hasAuthority('vlm:vehicle-correction:list')")
    public Result<PageResult<VehicleListResponse>> list(VehicleQueryRequest request) {
        return Result.success(vehicleCorrectionService.pageCorrections(request));
    }

    @GetMapping("/{vehicleId}")
    @PreAuthorize("hasAuthority('vlm:vehicle-correction:list')")
    public Result<VehiclePanoramaResponse> detail(@PathVariable Long vehicleId) {
        return Result.success(vehicleCorrectionService.getCorrection(vehicleId));
    }
}
