package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.dto.request.ModelCreateRequest;
import com.company.admin.dto.request.ModelQueryRequest;
import com.company.admin.dto.request.ModelUpdateRequest;
import com.company.admin.entity.VehicleModel;

import java.util.List;

/**
 * 车型数据管理服务接口
 */
public interface VehicleModelService {

    /** 车型分页列表 */
    PageResult<VehicleModel> pageModels(ModelQueryRequest request);

    /** 新增车型 */
    void createModel(ModelCreateRequest request);

    /** 编辑车型 */
    void updateModel(ModelUpdateRequest request);

    /** 删除车型 */
    void deleteModel(Long id);

    /** 启停车型 */
    void toggleModelStatus(Long id, Integer status);

    /** 获取所有启用车型（下拉用） */
    List<VehicleModel> listAllEnabled();

    /** 获取车系列表（去重，下拉用） */
    List<String> listSeries();
}