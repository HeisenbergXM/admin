package com.company.admin.controller;

import com.company.admin.annotation.OpLog;
import com.company.admin.common.PageResult;
import com.company.admin.common.Result;
import com.company.admin.dto.request.AllocationQueryRequest;
import com.company.admin.dto.request.AllocationSaveRequest;
import com.company.admin.dto.response.AllocationResponse;
import com.company.admin.service.VehAllocationService;
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
@RequestMapping("/api/allocations")
@RequiredArgsConstructor
@Tag(name = "车辆销售分配")
public class VehAllocationController {

    private final VehAllocationService vehAllocationService;

    @GetMapping
    @Operation(summary = "销售分配列表")
    @PreAuthorize("hasAuthority('vlm:allocation:list')")
    public Result<PageResult<AllocationResponse>> listAllocations(AllocationQueryRequest request) {
        return Result.success(vehAllocationService.pageAllocations(request));
    }

    @PostMapping("/{vehicleId}")
    @Operation(summary = "创建销售分配草稿")
    @PreAuthorize("hasAuthority('vlm:allocation:add')")
    @OpLog(value = "创建销售分配", type = OpLog.LogType.INSERT)
    public Result<Long> createAllocation(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId,
                                         @Valid @RequestBody AllocationSaveRequest request) {
        return Result.success(vehAllocationService.createAllocation(vehicleId, request));
    }

    @PutMapping("/{vehicleId}")
    @Operation(summary = "更新销售分配草稿")
    @PreAuthorize("hasAuthority('vlm:allocation:edit')")
    @OpLog(value = "更新销售分配", type = OpLog.LogType.UPDATE)
    public Result<Void> updateAllocation(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId,
                                         @Valid @RequestBody AllocationSaveRequest request) {
        vehAllocationService.updateAllocation(vehicleId, request);
        return Result.success();
    }

    @PostMapping("/{vehicleId}/confirm")
    @Operation(summary = "确认销售分配")
    @PreAuthorize("hasAuthority('vlm:allocation:confirm')")
    @OpLog(value = "确认销售分配", type = OpLog.LogType.UPDATE)
    public Result<Void> confirmAllocation(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId) {
        vehAllocationService.confirmAllocation(vehicleId);
        return Result.success();
    }

    @GetMapping("/{vehicleId}")
    @Operation(summary = "获取销售分配")
    @PreAuthorize("hasAuthority('vlm:allocation:list')")
    public Result<AllocationResponse> getAllocation(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId) {
        return Result.success(vehAllocationService.getAllocation(vehicleId));
    }
}
