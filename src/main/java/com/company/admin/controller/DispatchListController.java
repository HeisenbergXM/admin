package com.company.admin.controller;

import com.company.admin.annotation.OpLog;
import com.company.admin.common.PageResult;
import com.company.admin.common.Result;
import com.company.admin.dto.request.DispatchListQueryRequest;
import com.company.admin.dto.request.DispatchListSaveRequest;
import com.company.admin.dto.request.WaybillDealerConfirmRequest;
import com.company.admin.dto.request.WaybillDealerSaveRequest;
import com.company.admin.dto.request.WaybillSaveRequest;
import com.company.admin.dto.response.DispatchListResponse;
import com.company.admin.service.DispatchListService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Tag(name = "发车清单")
public class DispatchListController {

    private final DispatchListService dispatchListService;

    @GetMapping("/api/dispatch-lists")
    @Operation(summary = "发车清单列表")
    @PreAuthorize("hasAuthority('vlm:dispatch:list')")
    public Result<PageResult<DispatchListResponse>> listDispatchLists(DispatchListQueryRequest request) {
        return Result.success(dispatchListService.pageDispatchLists(request));
    }

    @PostMapping("/api/dispatch-lists")
    @Operation(summary = "创建发车清单")
    @PreAuthorize("hasAuthority('vlm:dispatch:add')")
    @OpLog(value = "创建发车清单", type = OpLog.LogType.INSERT)
    public Result<Long> createDispatchList(@Valid @RequestBody DispatchListSaveRequest request) {
        return Result.success(dispatchListService.createDispatchList(request));
    }

    @GetMapping("/api/dispatch-lists/{id}")
    @Operation(summary = "发车清单详情")
    @PreAuthorize("hasAuthority('vlm:dispatch:list')")
    public Result<DispatchListResponse> getDetail(@PathVariable Long id) {
        return Result.success(dispatchListService.getDetail(id));
    }

    @PostMapping("/api/dispatch-lists/{dispatchId}/waybills")
    @Operation(summary = "添加行车路单")
    @PreAuthorize("hasAuthority('vlm:dispatch:edit')")
    @OpLog(value = "添加行车路单", type = OpLog.LogType.INSERT)
    public Result<Long> addWaybill(@PathVariable Long dispatchId,
                                   @Valid @RequestBody WaybillSaveRequest request) {
        return Result.success(dispatchListService.addWaybill(dispatchId, request));
    }

    @PutMapping("/api/waybills/{id}")
    @Operation(summary = "编辑行车路单")
    @PreAuthorize("hasAuthority('vlm:dispatch:edit')")
    @OpLog(value = "编辑行车路单", type = OpLog.LogType.UPDATE)
    public Result<Void> updateWaybill(@PathVariable Long id,
                                      @Valid @RequestBody WaybillSaveRequest request) {
        dispatchListService.updateWaybill(id, request);
        return Result.success();
    }

    @DeleteMapping("/api/waybills/{id}")
    @Operation(summary = "删除行车路单")
    @PreAuthorize("hasAuthority('vlm:dispatch:edit')")
    @OpLog(value = "删除行车路单", type = OpLog.LogType.DELETE)
    public Result<Void> deleteWaybill(@PathVariable Long id) {
        dispatchListService.deleteWaybill(id);
        return Result.success();
    }

    @PostMapping("/api/waybills/{waybillId}/dealers")
    @Operation(summary = "添加经销商行")
    @PreAuthorize("hasAuthority('vlm:dispatch:edit')")
    @OpLog(value = "添加经销商行", type = OpLog.LogType.INSERT)
    public Result<Long> addDealer(@PathVariable Long waybillId,
                                  @Valid @RequestBody WaybillDealerSaveRequest request) {
        return Result.success(dispatchListService.addDealer(waybillId, request));
    }

    @PutMapping("/api/waybill-dealers/{id}")
    @Operation(summary = "编辑经销商行")
    @PreAuthorize("hasAuthority('vlm:dispatch:edit')")
    @OpLog(value = "编辑经销商行", type = OpLog.LogType.UPDATE)
    public Result<Void> updateDealer(@PathVariable Long id,
                                     @Valid @RequestBody WaybillDealerSaveRequest request) {
        dispatchListService.updateDealer(id, request);
        return Result.success();
    }

    @DeleteMapping("/api/waybill-dealers/{id}")
    @Operation(summary = "删除经销商行")
    @PreAuthorize("hasAuthority('vlm:dispatch:edit')")
    @OpLog(value = "删除经销商行", type = OpLog.LogType.DELETE)
    public Result<Void> deleteDealer(@PathVariable Long id) {
        dispatchListService.deleteDealer(id);
        return Result.success();
    }

    @PostMapping("/api/waybill-dealers/{id}/vins")
    @Operation(summary = "挂载 VIN")
    @PreAuthorize("hasAuthority('vlm:dispatch:edit')")
    @OpLog(value = "经销商行挂载 VIN", type = OpLog.LogType.INSERT)
    public Result<Long> addVin(@PathVariable Long id,
                               @Parameter(description = "车辆 ID") @RequestParam Long vehicleId) {
        return Result.success(dispatchListService.addVin(id, vehicleId));
    }

    @DeleteMapping("/api/waybill-dealers/{id}/vins/{vinId}")
    @Operation(summary = "移除 VIN")
    @PreAuthorize("hasAuthority('vlm:dispatch:edit')")
    @OpLog(value = "经销商行移除 VIN", type = OpLog.LogType.DELETE)
    public Result<Void> removeVin(@PathVariable Long id, @PathVariable Long vinId) {
        dispatchListService.removeVin(id, vinId);
        return Result.success();
    }

    @PostMapping("/api/waybill-dealers/{id}/confirm")
    @Operation(summary = "经销商行签收确认")
    @PreAuthorize("hasAuthority('vlm:dispatch:confirm')")
    @OpLog(value = "经销商行签收确认", type = OpLog.LogType.UPDATE)
    public Result<Void> confirmDealerRow(@PathVariable Long id,
                                         @RequestBody(required = false) WaybillDealerConfirmRequest request) {
        dispatchListService.confirmDealerRow(id, request);
        return Result.success();
    }
}
