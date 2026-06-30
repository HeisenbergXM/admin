package com.company.admin.controller;

import com.company.admin.annotation.OpLog;
import com.company.admin.common.PageResult;
import com.company.admin.common.Result;
import com.company.admin.dto.request.PaymentQueryRequest;
import com.company.admin.dto.request.PaymentSaveRequest;
import com.company.admin.dto.response.PaymentResponse;
import com.company.admin.service.VehPaymentService;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "收款确认")
public class VehPaymentController {

    private final VehPaymentService vehPaymentService;

    @GetMapping
    @Operation(summary = "收款列表")
    @PreAuthorize("hasAuthority('vlm:payment:list')")
    public Result<PageResult<PaymentResponse>> listPayments(PaymentQueryRequest request) {
        return Result.success(vehPaymentService.pagePayments(request));
    }

    @PostMapping("/{vehicleId}")
    @Operation(summary = "创建收款草稿")
    @PreAuthorize("hasAuthority('vlm:payment:add')")
    @OpLog(value = "创建收款", type = OpLog.LogType.INSERT)
    public Result<Long> createPayment(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId,
                                      @Valid @RequestBody PaymentSaveRequest request) {
        return Result.success(vehPaymentService.createPayment(vehicleId, request));
    }

    @PutMapping("/{vehicleId}")
    @Operation(summary = "更新收款草稿")
    @PreAuthorize("hasAuthority('vlm:payment:edit')")
    @OpLog(value = "更新收款", type = OpLog.LogType.UPDATE)
    public Result<Void> updatePayment(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId,
                                      @Valid @RequestBody PaymentSaveRequest request) {
        vehPaymentService.updatePayment(vehicleId, request);
        return Result.success();
    }

    @PostMapping("/{vehicleId}/confirm")
    @Operation(summary = "确认收款")
    @PreAuthorize("hasAuthority('vlm:payment:confirm')")
    @OpLog(value = "确认收款", type = OpLog.LogType.UPDATE)
    public Result<Void> confirmPayment(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId) {
        vehPaymentService.confirmPayment(vehicleId);
        return Result.success();
    }

    @GetMapping("/{vehicleId}")
    @Operation(summary = "获取收款记录")
    @PreAuthorize("hasAuthority('vlm:payment:list')")
    public Result<PaymentResponse> getPayment(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId) {
        return Result.success(vehPaymentService.getPayment(vehicleId));
    }
}
