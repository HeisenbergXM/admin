package com.company.admin.controller;

import com.company.admin.annotation.OpLog;
import com.company.admin.common.PageResult;
import com.company.admin.common.Result;
import com.company.admin.dto.request.DeliveryQueryRequest;
import com.company.admin.dto.request.DeliverySaveRequest;
import com.company.admin.dto.response.DeliveryResponse;
import com.company.admin.service.VehDeliveryService;
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
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@Tag(name = "单车配送")
public class VehDeliveryController {

    private final VehDeliveryService vehDeliveryService;

    @GetMapping
    @Operation(summary = "配送列表")
    @PreAuthorize("hasAuthority('vlm:delivery:list')")
    public Result<PageResult<DeliveryResponse>> listDeliveries(DeliveryQueryRequest request) {
        return Result.success(vehDeliveryService.pageDeliveries(request));
    }

    @PostMapping("/{vehicleId}")
    @Operation(summary = "创建单车配送草稿")
    @PreAuthorize("hasAuthority('vlm:delivery:add')")
    @OpLog(value = "创建单车配送草稿", type = OpLog.LogType.INSERT)
    public Result<Long> createDelivery(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId,
                                       @Valid @RequestBody DeliverySaveRequest request) {
        return Result.success(vehDeliveryService.createDelivery(vehicleId, request));
    }

    @PutMapping("/{vehicleId}")
    @Operation(summary = "更新单车配送草稿")
    @PreAuthorize("hasAuthority('vlm:delivery:edit')")
    @OpLog(value = "更新单车配送草稿", type = OpLog.LogType.UPDATE)
    public Result<Void> updateDelivery(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId,
                                       @Valid @RequestBody DeliverySaveRequest request) {
        vehDeliveryService.updateDelivery(vehicleId, request);
        return Result.success();
    }

    @PostMapping("/{vehicleId}/confirm")
    @Operation(summary = "确认单车配送签收")
    @PreAuthorize("hasAuthority('vlm:delivery:confirm')")
    @OpLog(value = "确认单车配送签收", type = OpLog.LogType.UPDATE)
    public Result<Void> confirmDelivery(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId) {
        vehDeliveryService.confirmDelivery(vehicleId);
        return Result.success();
    }

    @GetMapping("/{vehicleId}")
    @Operation(summary = "获取单车配送记录")
    @PreAuthorize("hasAuthority('vlm:delivery:list')")
    public Result<DeliveryResponse> getDelivery(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId) {
        return Result.success(vehDeliveryService.getDelivery(vehicleId));
    }
}
