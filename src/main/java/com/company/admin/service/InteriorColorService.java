package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.dto.request.InteriorColorCreateRequest;
import com.company.admin.dto.request.InteriorColorQueryRequest;
import com.company.admin.dto.request.InteriorColorUpdateRequest;
import com.company.admin.entity.InteriorColor;

import java.util.List;

/**
 * 内饰颜色管理服务接口
 */
public interface InteriorColorService {

    /** 内饰颜色分页列表 */
    PageResult<InteriorColor> pageInteriorColors(InteriorColorQueryRequest request);

    /** 新增内饰颜色 */
    void createInteriorColor(InteriorColorCreateRequest request);

    /** 编辑内饰颜色 */
    void updateInteriorColor(InteriorColorUpdateRequest request);

    /** 删除内饰颜色 */
    void deleteInteriorColor(Long id);

    /** 启停内饰颜色 */
    void toggleInteriorColorStatus(Long id, Integer status);

    /** 获取所有启用内饰颜色（下拉用） */
    List<InteriorColor> listAllEnabled();
}