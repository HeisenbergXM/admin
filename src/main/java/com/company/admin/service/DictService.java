package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.dto.request.DictItemSaveRequest;
import com.company.admin.dto.request.DictTypeCreateRequest;
import com.company.admin.dto.request.DictTypeQueryRequest;
import com.company.admin.dto.request.DictTypeUpdateRequest;
import com.company.admin.entity.DictItem;
import com.company.admin.entity.DictType;

import java.util.List;

/**
 * 字典管理服务接口
 */
public interface DictService {

    // ========== 字典类型 ==========

    /** 字典类型分页列表（支持按编码/名称查询） */
    PageResult<DictType> pageDictTypes(DictTypeQueryRequest request);

    /** 新增字典类型 */
    void createDictType(DictTypeCreateRequest request);

    /** 编辑字典类型 */
    void updateDictType(DictTypeUpdateRequest request);

    /** 删除字典类型（有关联字典项时拒绝） */
    void deleteDictType(Long id);

    /** 启停字典类型 */
    void toggleDictTypeStatus(Long id, Integer status);

    // ========== 字典项 ==========

    /** 获取字典类型下所有字典项（排序后） */
    List<DictItem> listDictItems(Long dictTypeId);

    /** 新增字典项 */
    void createDictItem(DictItemSaveRequest request);

    /** 编辑字典项 */
    void updateDictItem(DictItemSaveRequest request);

    /** 删除字典项 */
    void deleteDictItem(Long id);

    /** 启停字典项 */
    void toggleDictItemStatus(Long id, Integer status);

    // ========== 公共查询 ==========

    /** 按字典编码获取字典项列表（仅返回正常状态，供前端下拉选择用） */
    List<DictItem> getItemsByCode(String dictCode);
}
