package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.dto.request.InboundQueryRequest;
import com.company.admin.dto.response.InboundResponse;
import com.company.admin.entity.VehInbound;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VehInboundMapper extends BaseMapper<VehInbound> {

    Page<InboundResponse> selectInboundPage(Page<InboundResponse> page,
                                            @Param("query") InboundQueryRequest query);

    InboundResponse selectInboundByVehicleId(@Param("vehicleId") Long vehicleId);

    VehInbound selectByVehicleIdForUpdate(@Param("vehicleId") Long vehicleId);
}
