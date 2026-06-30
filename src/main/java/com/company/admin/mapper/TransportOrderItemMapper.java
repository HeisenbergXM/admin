package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.admin.entity.TransportOrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TransportOrderItemMapper extends BaseMapper<TransportOrderItem> {

    Long countOpenOccupancy(@Param("vehicleId") Long vehicleId,
                            @Param("orderId") Long orderId);

    List<TransportOrderItem> selectByOrderId(@Param("orderId") Long orderId);
}
