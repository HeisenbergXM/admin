package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.dto.request.InvoiceConvertRequest;
import com.company.admin.dto.request.InvoiceCreateRequest;
import com.company.admin.dto.request.InvoiceQueryRequest;
import com.company.admin.dto.response.InvoiceListResponse;
import com.company.admin.dto.response.InvoiceResponse;

import java.util.List;

public interface VehInvoiceService {

    PageResult<InvoiceListResponse> pageInvoices(InvoiceQueryRequest request);

    Long createInvoice(Long vehicleId, InvoiceCreateRequest request);

    void updateInvoice(Long id, InvoiceCreateRequest request);

    void confirmInvoice(Long id);

    Long convertProforma(Long vehicleId, InvoiceConvertRequest request);

    List<InvoiceResponse> getInvoices(Long vehicleId);
}
