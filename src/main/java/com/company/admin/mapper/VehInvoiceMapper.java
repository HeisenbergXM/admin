package com.company.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.dto.request.InvoiceQueryRequest;
import com.company.admin.dto.response.InvoiceListResponse;
import com.company.admin.entity.VehInvoice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VehInvoiceMapper extends BaseMapper<VehInvoice> {

    Page<InvoiceListResponse> selectInvoicePage(Page<InvoiceListResponse> page,
                                                @Param("query") InvoiceQueryRequest query);

    VehInvoice selectByIdForUpdate(@Param("id") Long id);
}
