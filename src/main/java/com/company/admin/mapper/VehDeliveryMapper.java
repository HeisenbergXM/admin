package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.dto.request.DeliveryQueryRequest;
import com.company.admin.dto.response.DeliveryResponse;
import com.company.admin.entity.VehDelivery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VehDeliveryMapper extends BaseMapper<VehDelivery> {

    Page<DeliveryResponse> selectDeliveryPage(Page<DeliveryResponse> page,
                                              @Param("query") DeliveryQueryRequest query);

    DeliveryResponse selectDeliveryByVehicleId(@Param("vehicleId") Long vehicleId);
}
