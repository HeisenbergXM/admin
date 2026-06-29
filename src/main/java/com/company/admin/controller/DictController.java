package com.company.admin.controller;

import com.company.admin.annotation.OpLog;
import com.company.admin.common.PageResult;
import com.company.admin.common.Result;
import com.company.admin.dto.request.DictItemSaveRequest;
import com.company.admin.dto.request.DictTypeCreateRequest;
import com.company.admin.dto.request.DictTypeQueryRequest;
import com.company.admin.dto.request.DictTypeUpdateRequest;
import com.company.admin.dto.request.StatusUpdateRequest;
import com.company.admin.entity.DictItem;
import com.company.admin.entity.DictType;
import com.company.admin.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 字典管理控制器
 */
@RestController
@RequestMapping("/api/dict")
@Tag(name = "字典管理")
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    // ==================== 字典类型 ====================

    @GetMapping("/types")
    @Operation(summary = "字典类型分页列表")
    @PreAuthorize("hasAuthority('sys:dict:list')")
    public Result<PageResult<DictType>> listTypes(DictTypeQueryRequest request) {
        return Result.success(dictService.pageDictTypes(request));
    }

    @PostMapping("/types")
    @Operation(summary = "新增字典类型")
    @PreAuthorize("hasAuthority('sys:dict:add')")
    @OpLog(value = "新增字典类型", type = OpLog.LogType.INSERT)
    public Result<Void> createType(@Valid @RequestBody DictTypeCreateRequest request) {
        dictService.createDictType(request);
        return Result.success();
    }

    @PutMapping("/types/{id}")
    @Operation(summary = "编辑字典类型")
    @PreAuthorize("hasAuthority('sys:dict:edit')")
    @OpLog(value = "编辑字典类型", type = OpLog.LogType.UPDATE)
    public Result<Void> updateType(@Parameter(description = "主键 ID") @PathVariable Long id,
                                   @Valid @RequestBody DictTypeUpdateRequest request) {
        request.setId(id);
        dictService.updateDictType(request);
        return Result.success();
    }

    @DeleteMapping("/types/{id}")
    @Operation(summary = "删除字典类型")
    @PreAuthorize("hasAuthority('sys:dict:delete')")
    @OpLog(value = "删除字典类型", type = OpLog.LogType.DELETE)
    public Result<Void> deleteType(@Parameter(description = "主键 ID") @PathVariable Long id) {
        dictService.deleteDictType(id);
        return Result.success();
    }

    @PutMapping("/types/{id}/status")
    @Operation(summary = "启停字典类型")
    @PreAuthorize("hasAuthority('sys:dict:edit')")
    @OpLog(value = "启停字典类型", type = OpLog.LogType.UPDATE)
    public Result<Void> toggleTypeStatus(@Parameter(description = "主键 ID") @PathVariable Long id,
                                         @Valid @RequestBody StatusUpdateRequest request) {
        dictService.toggleDictTypeStatus(id, request.getStatus());
        return Result.success();
    }

    // ==================== 字典项 ====================

    @GetMapping("/types/{typeId}/items")
    @Operation(summary = "字典项列表")
    @PreAuthorize("hasAuthority('sys:dict:list')")
    public Result<List<DictItem>> listItems(@Parameter(description = "字典类型 ID") @PathVariable Long typeId) {
        return Result.success(dictService.listDictItems(typeId));
    }

    @PostMapping("/items")
    @Operation(summary = "新增字典项")
    @PreAuthorize("hasAuthority('sys:dict:add')")
    @OpLog(value = "新增字典项", type = OpLog.LogType.INSERT)
    public Result<Void> createItem(@Valid @RequestBody DictItemSaveRequest request) {
        dictService.createDictItem(request);
        return Result.success();
    }

    @PutMapping("/items/{id}")
    @Operation(summary = "编辑字典项")
    @PreAuthorize("hasAuthority('sys:dict:edit')")
    @OpLog(value = "编辑字典项", type = OpLog.LogType.UPDATE)
    public Result<Void> updateItem(@Parameter(description = "主键 ID") @PathVariable Long id,
                                   @Valid @RequestBody DictItemSaveRequest request) {
        request.setId(id);
        dictService.updateDictItem(request);
        return Result.success();
    }

    @DeleteMapping("/items/{id}")
    @Operation(summary = "删除字典项")
    @PreAuthorize("hasAuthority('sys:dict:delete')")
    @OpLog(value = "删除字典项", type = OpLog.LogType.DELETE)
    public Result<Void> deleteItem(@Parameter(description = "主键 ID") @PathVariable Long id) {
        dictService.deleteDictItem(id);
        return Result.success();
    }

    @PutMapping("/items/{id}/status")
    @Operation(summary = "启停字典项")
    @PreAuthorize("hasAuthority('sys:dict:edit')")
    @OpLog(value = "启停字典项", type = OpLog.LogType.UPDATE)
    public Result<Void> toggleItemStatus(@Parameter(description = "主键 ID") @PathVariable Long id,
                                         @Valid @RequestBody StatusUpdateRequest request) {
        dictService.toggleDictItemStatus(id, request.getStatus());
        return Result.success();
    }

    // ==================== 公共查询（前端下拉用） ====================

    @GetMapping("/code/{dictCode}")
    @Operation(summary = "按字典编码获取字典项列表（前端下拉用）")
    public Result<List<DictItem>> getItemsByCode(@Parameter(description = "字典编码") @PathVariable String dictCode) {
        return Result.success(dictService.getItemsByCode(dictCode));
    }
}
