package com.company.admin.controller;

import com.company.admin.annotation.OpLog;
import com.company.admin.common.PageResult;
import com.company.admin.common.Result;
import com.company.admin.dto.request.DealerCreateRequest;
import com.company.admin.dto.request.DealerQueryRequest;
import com.company.admin.dto.request.DealerUpdateRequest;
import com.company.admin.dto.request.StatusUpdateRequest;
import com.company.admin.entity.Dealer;
import com.company.admin.service.DealerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 经销商控制器
 */
@RestController
@RequestMapping("/api/master-data/dealers")
@Tag(name = "经销商")
public class DealerController {

    private final DealerService dealerService;

    public DealerController(DealerService dealerService) {
        this.dealerService = dealerService;
    }

    @GetMapping
    @Operation(summary = "经销商分页列表")
    @PreAuthorize("hasAuthority('sys:master:list')")
    public Result<PageResult<Dealer>> listDealers(DealerQueryRequest request) {
        return Result.success(dealerService.pageDealers(request));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有启用经销商（下拉用）")
    public Result<List<Dealer>> listAllEnabled() {
        return Result.success(dealerService.listAllEnabled());
    }

    @PostMapping
    @Operation(summary = "新增经销商")
    @PreAuthorize("hasAuthority('sys:master:add')")
    @OpLog(value = "新增经销商", type = OpLog.LogType.INSERT)
    public Result<Void> createDealer(@Valid @RequestBody DealerCreateRequest request) {
        dealerService.createDealer(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑经销商")
    @PreAuthorize("hasAuthority('sys:master:edit')")
    @OpLog(value = "编辑经销商", type = OpLog.LogType.UPDATE)
    public Result<Void> updateDealer(@Parameter(description = "主键 ID") @PathVariable Long id,
                                     @Valid @RequestBody DealerUpdateRequest request) {
        request.setId(id);
        dealerService.updateDealer(request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除经销商")
    @PreAuthorize("hasAuthority('sys:master:delete')")
    @OpLog(value = "删除经销商", type = OpLog.LogType.DELETE)
    public Result<Void> deleteDealer(@Parameter(description = "主键 ID") @PathVariable Long id) {
        dealerService.deleteDealer(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "启停经销商")
    @PreAuthorize("hasAuthority('sys:master:edit')")
    @OpLog(value = "启停经销商", type = OpLog.LogType.UPDATE)
    public Result<Void> toggleDealerStatus(@Parameter(description = "主键 ID") @PathVariable Long id,
                                           @Valid @RequestBody StatusUpdateRequest request) {
        dealerService.toggleDealerStatus(id, request.getStatus());
        return Result.success();
    }
}
