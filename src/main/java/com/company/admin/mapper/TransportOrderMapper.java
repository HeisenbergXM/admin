package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.dto.request.TransportOrderQueryRequest;
import com.company.admin.dto.response.TransportOrderListResponse;
import com.company.admin.entity.TransportOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TransportOrderMapper extends BaseMapper<TransportOrder> {

    Page<TransportOrderListResponse> selectOrderPage(Page<TransportOrderListResponse> page,
                                                     @Param("query") TransportOrderQueryRequest query);
}
