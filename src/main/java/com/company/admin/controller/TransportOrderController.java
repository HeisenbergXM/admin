package com.company.admin.controller;

import com.company.admin.annotation.OpLog;
import com.company.admin.common.PageResult;
import com.company.admin.common.Result;
import com.company.admin.dto.request.TransportOrderItemRequest;
import com.company.admin.dto.request.TransportOrderQueryRequest;
import com.company.admin.dto.request.TransportOrderSaveRequest;
import com.company.admin.dto.response.TransportOrderDetailResponse;
import com.company.admin.dto.response.TransportOrderListResponse;
import com.company.admin.service.TransportOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/transport-orders")
@RequiredArgsConstructor
@Tag(name = "运输单-车厂到仓库")
public class TransportOrderController {

    private final TransportOrderService transportOrderService;

    @GetMapping
    @Operation(summary = "运输单分页列表")
    @PreAuthorize("hasAuthority('vlm:transport:list')")
    public Result<PageResult<TransportOrderListResponse>> listOrders(TransportOrderQueryRequest request) {
        return Result.success(transportOrderService.pageOrders(request));
    }

    @PostMapping
    @Operation(summary = "创建运输单草稿")
    @PreAuthorize("hasAuthority('vlm:transport:add')")
    @OpLog(value = "创建运输单", type = OpLog.LogType.INSERT)
    public Result<Long> createOrder(@Valid @RequestBody TransportOrderSaveRequest request) {
        return Result.success(transportOrderService.createOrder(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑运输单表头")
    @PreAuthorize("hasAuthority('vlm:transport:edit')")
    @OpLog(value = "编辑运输单", type = OpLog.LogType.UPDATE)
    public Result<Void> updateOrder(@Parameter(description = "运输单 ID") @PathVariable Long id,
                                    @Valid @RequestBody TransportOrderSaveRequest request) {
        transportOrderService.updateOrder(id, request);
        return Result.success();
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "添加 VIN 明细")
    @PreAuthorize("hasAuthority('vlm:transport:edit')")
    @OpLog(value = "运输单添加 VIN", type = OpLog.LogType.INSERT)
    public Result<Long> addItem(@Parameter(description = "运输单 ID") @PathVariable Long id,
                                @Valid @RequestBody TransportOrderItemRequest request) {
        return Result.success(transportOrderService.addItem(id, request));
    }

    @PutMapping("/{id}/items/{itemId}")
    @Operation(summary = "更新 VIN 明细")
    @PreAuthorize("hasAuthority('vlm:transport:edit')")
    @OpLog(value = "更新运输单 VIN", type = OpLog.LogType.UPDATE)
    public Result<Void> updateItem(@PathVariable Long id,
                                   @PathVariable Long itemId,
                                   @Valid @RequestBody TransportOrderItemRequest request) {
        transportOrderService.updateItem(id, itemId, request);
        return Result.success();
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @Operation(summary = "移除 VIN 明细")
    @PreAuthorize("hasAuthority('vlm:transport:edit')")
    @OpLog(value = "移除运输单 VIN", type = OpLog.LogType.DELETE)
    public Result<Void> removeItem(@PathVariable Long id, @PathVariable Long itemId) {
        transportOrderService.removeItem(id, itemId);
        return Result.success();
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "确认运输单")
    @PreAuthorize("hasAuthority('vlm:transport:confirm')")
    @OpLog(value = "确认运输单", type = OpLog.LogType.UPDATE)
    public Result<Void> confirmOrder(@PathVariable Long id) {
        transportOrderService.confirmOrder(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "运输单详情")
    @PreAuthorize("hasAuthority('vlm:transport:list')")
    public Result<TransportOrderDetailResponse> getDetail(@PathVariable Long id) {
        return Result.success(transportOrderService.getDetail(id));
    }
}
