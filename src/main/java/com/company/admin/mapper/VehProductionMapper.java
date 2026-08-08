package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.dto.request.ProductionQueryRequest;
import com.company.admin.dto.response.VehicleListResponse;
import com.company.admin.entity.VehProduction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VehProductionMapper extends BaseMapper<VehProduction> {

    VehProduction selectByIdForUpdate(@Param("id") Long id);

    /**
     * 生产录入待办分页查询：固定 v.lifecycle_stage = 'PENDING_OFFLINE'。
     */
    Page<VehicleListResponse> selectProductionPage(Page<VehicleListResponse> page,
                                                   @Param("query") ProductionQueryRequest query);
}
