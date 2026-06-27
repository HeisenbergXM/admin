package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.dto.request.ExteriorColorCreateRequest;
import com.company.admin.dto.request.ExteriorColorQueryRequest;
import com.company.admin.dto.request.ExteriorColorUpdateRequest;
import com.company.admin.entity.ExteriorColor;

import java.util.List;

/**
 * 外饰颜色管理服务接口
 */
public interface ExteriorColorService {

    /** 外饰颜色分页列表 */
    PageResult<ExteriorColor> pageExteriorColors(ExteriorColorQueryRequest request);

    /** 新增外饰颜色 */
    void createExteriorColor(ExteriorColorCreateRequest request);

    /** 编辑外饰颜色 */
    void updateExteriorColor(ExteriorColorUpdateRequest request);

    /** 删除外饰颜色 */
    void deleteExteriorColor(Long id);

    /** 启停外饰颜色 */
    void toggleExteriorColorStatus(Long id, Integer status);

    /** 获取所有启用外饰颜色（下拉用） */
    List<ExteriorColor> listAllEnabled();
}