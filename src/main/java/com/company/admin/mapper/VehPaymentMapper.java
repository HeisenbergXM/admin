package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.dto.request.PaymentQueryRequest;
import com.company.admin.dto.response.PaymentResponse;
import com.company.admin.entity.VehPayment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VehPaymentMapper extends BaseMapper<VehPayment> {

    Page<PaymentResponse> selectPaymentPage(Page<PaymentResponse> page,
                                            @Param("query") PaymentQueryRequest query);
}
