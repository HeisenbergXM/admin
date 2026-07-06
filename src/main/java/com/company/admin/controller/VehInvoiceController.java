package com.company.admin.controller;

import com.company.admin.annotation.OpLog;
import com.company.admin.common.PageResult;
import com.company.admin.common.Result;
import com.company.admin.dto.request.InvoiceConvertRequest;
import com.company.admin.dto.request.InvoiceCreateRequest;
import com.company.admin.dto.request.InvoiceQueryRequest;
import com.company.admin.dto.response.InvoiceListResponse;
import com.company.admin.dto.response.InvoiceResponse;
import com.company.admin.service.VehInvoiceService;
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
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "发票确认")
public class VehInvoiceController {

    private final VehInvoiceService vehInvoiceService;

    @GetMapping
    @Operation(summary = "发票列表")
    @PreAuthorize("hasAuthority('vlm:invoice:list')")
    public Result<PageResult<InvoiceListResponse>> listInvoices(InvoiceQueryRequest request) {
        return Result.success(vehInvoiceService.pageInvoices(request));
    }

    @PostMapping("/{vehicleId}")
    @Operation(summary = "保存发票草稿")
    @PreAuthorize("hasAuthority('vlm:invoice:add')")
    @OpLog(value = "保存发票草稿", type = OpLog.LogType.INSERT)
    public Result<Long> createInvoice(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId,
                                      @Valid @RequestBody InvoiceCreateRequest request) {
        return Result.success(vehInvoiceService.createInvoice(vehicleId, request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新发票草稿")
    @PreAuthorize("hasAuthority('vlm:invoice:edit')")
    @OpLog(value = "更新发票", type = OpLog.LogType.UPDATE)
    public Result<Void> updateInvoice(@Parameter(description = "发票 ID") @PathVariable Long id,
                                      @Valid @RequestBody InvoiceCreateRequest request) {
        vehInvoiceService.updateInvoice(id, request);
        return Result.success();
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "确认发票")
    @PreAuthorize("hasAuthority('vlm:invoice:confirm')")
    @OpLog(value = "确认发票", type = OpLog.LogType.UPDATE)
    public Result<Void> confirmInvoice(@Parameter(description = "发票 ID") @PathVariable Long id) {
        vehInvoiceService.confirmInvoice(id);
        return Result.success();
    }

    @PostMapping("/{vehicleId}/convert")
    @Operation(summary = "保存形式发票转正式草稿")
    @PreAuthorize("hasAuthority('vlm:invoice:convert')")
    @OpLog(value = "保存形式发票转正式草稿", type = OpLog.LogType.INSERT)
    public Result<Long> convertProforma(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId,
                                        @Valid @RequestBody InvoiceConvertRequest request) {
        return Result.success(vehInvoiceService.convertProforma(vehicleId, request));
    }

    @GetMapping("/{vehicleId}")
    @Operation(summary = "查询车辆发票记录")
    @PreAuthorize("hasAuthority('vlm:invoice:list')")
    public Result<List<InvoiceResponse>> getInvoices(@Parameter(description = "车辆 ID") @PathVariable Long vehicleId) {
        return Result.success(vehInvoiceService.getInvoices(vehicleId));
    }
}
