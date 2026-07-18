package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.dto.request.VehicleQueryRequest;
import com.company.admin.dto.response.VehicleBasicInfo;
import com.company.admin.dto.response.VehicleListResponse;
import com.company.admin.entity.Vehicle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VehicleMapper extends BaseMapper<Vehicle> {

    Page<VehicleListResponse> selectVehiclePage(Page<VehicleListResponse> page,
                                                @Param("query") VehicleQueryRequest query);

    Page<VehicleListResponse> selectVehicleCorrectionPage(Page<VehicleListResponse> page,
                                                          @Param("query") VehicleQueryRequest query);

    /**
     * 按 ID 查询车辆基本信息（JOIN 主数据表取车型/颜色名称）
     */
    VehicleBasicInfo selectBasicInfoById(@Param("vehicleId") Long vehicleId);

    /**
     * 批量按 ID 查询车辆基本信息
     */
    List<VehicleBasicInfo> selectBasicInfoByIds(@Param("ids") List<Long> ids);

    /**
     * 按阶段查询候选车辆（VIN 候选选择器用）
     */
    List<VehicleBasicInfo> selectCandidates(@Param("stage") String stage,
                                            @Param("vinPattern") String vinPattern);
}
