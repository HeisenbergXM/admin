package com.company.admin.controller;

import com.company.admin.annotation.OpLog;
import com.company.admin.common.PageResult;
import com.company.admin.common.Result;
import com.company.admin.dto.request.InboundQueryRequest;
import com.company.admin.dto.request.InboundSaveRequest;
import com.company.admin.dto.response.InboundResponse;
import com.company.admin.service.VehInboundService;
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
@RequestMapping("/api/inbounds")
@RequiredArgsConstructor
@Tag(name = "单车入库")
public class VehInboundController {

    private final VehInboundService vehInboundService;

    @GetMapping
    @Operation(summary = "入库列表")
    @PreAuthorize("hasAuthority('vlm:inbound:list')")
    public Result<PageResult<InboundResponse>> listInbounds(InboundQueryRequest request) {
        return Result.success(vehInboundService.pageInbounds(request));
    }

    @PostMapping("/{vehicleId}")
    @Operation(summary = "创建单车入库草稿")
    @PreAuthorize("hasAuthority('vlm:inbound:add')")
    @OpLog(value = "创建单车入库草稿", type = OpLog.LogType.INSERT)
    public Result<Long> createInbound(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId,
                                      @Valid @RequestBody InboundSaveRequest request) {
        return Result.success(vehInboundService.createInbound(vehicleId, request));
    }

    @PutMapping("/{vehicleId}")
    @Operation(summary = "更新单车入库草稿")
    @PreAuthorize("hasAuthority('vlm:inbound:edit')")
    @OpLog(value = "更新单车入库草稿", type = OpLog.LogType.UPDATE)
    public Result<Void> updateInbound(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId,
                                      @Valid @RequestBody InboundSaveRequest request) {
        vehInboundService.updateInbound(vehicleId, request);
        return Result.success();
    }

    @PostMapping("/{vehicleId}/confirm")
    @Operation(summary = "确认单车入库")
    @PreAuthorize("hasAuthority('vlm:inbound:confirm')")
    @OpLog(value = "确认单车入库", type = OpLog.LogType.UPDATE)
    public Result<Void> confirmInbound(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId) {
        vehInboundService.confirmInbound(vehicleId);
        return Result.success();
    }

    @GetMapping("/{vehicleId}")
    @Operation(summary = "获取单车入库记录")
    @PreAuthorize("hasAuthority('vlm:inbound:list')")
    public Result<InboundResponse> getInbound(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId) {
        return Result.success(vehInboundService.getInbound(vehicleId));
    }
}
