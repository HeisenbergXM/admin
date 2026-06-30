package com.company.admin.controller;

import com.company.admin.annotation.OpLog;
import com.company.admin.common.PageResult;
import com.company.admin.common.Result;
import com.company.admin.dto.request.RegistrationQueryRequest;
import com.company.admin.dto.request.RegistrationSaveRequest;
import com.company.admin.dto.response.RegistrationResponse;
import com.company.admin.service.VehRegistrationService;
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
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
@Tag(name = "车辆上牌")
public class VehRegistrationController {

    private final VehRegistrationService vehRegistrationService;

    @GetMapping
    @Operation(summary = "上牌列表")
    @PreAuthorize("hasAuthority('vlm:registration:list')")
    public Result<PageResult<RegistrationResponse>> listRegistrations(RegistrationQueryRequest request) {
        return Result.success(vehRegistrationService.pageRegistrations(request));
    }

    @PostMapping("/{vehicleId}")
    @Operation(summary = "创建上牌草稿")
    @PreAuthorize("hasAuthority('vlm:registration:add')")
    @OpLog(value = "创建上牌", type = OpLog.LogType.INSERT)
    public Result<Long> createRegistration(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId,
                                           @Valid @RequestBody RegistrationSaveRequest request) {
        return Result.success(vehRegistrationService.createRegistration(vehicleId, request));
    }

    @PutMapping("/{vehicleId}")
    @Operation(summary = "更新上牌草稿")
    @PreAuthorize("hasAuthority('vlm:registration:edit')")
    @OpLog(value = "更新上牌", type = OpLog.LogType.UPDATE)
    public Result<Void> updateRegistration(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId,
                                           @Valid @RequestBody RegistrationSaveRequest request) {
        vehRegistrationService.updateRegistration(vehicleId, request);
        return Result.success();
    }

    @PostMapping("/{vehicleId}/confirm")
    @Operation(summary = "确认上牌")
    @PreAuthorize("hasAuthority('vlm:registration:confirm')")
    @OpLog(value = "确认上牌", type = OpLog.LogType.UPDATE)
    public Result<Void> confirmRegistration(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId) {
        vehRegistrationService.confirmRegistration(vehicleId);
        return Result.success();
    }

    @GetMapping("/{vehicleId}")
    @Operation(summary = "获取上牌记录")
    @PreAuthorize("hasAuthority('vlm:registration:list')")
    public Result<RegistrationResponse> getRegistration(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId) {
        return Result.success(vehRegistrationService.getRegistration(vehicleId));
    }
}
