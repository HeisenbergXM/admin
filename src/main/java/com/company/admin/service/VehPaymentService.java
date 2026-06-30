package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.dto.request.PaymentQueryRequest;
import com.company.admin.dto.request.PaymentSaveRequest;
import com.company.admin.dto.response.PaymentResponse;

public interface VehPaymentService {

    PageResult<PaymentResponse> pagePayments(PaymentQueryRequest request);

    Long createPayment(Long vehicleId, PaymentSaveRequest request);

    void updatePayment(Long vehicleId, PaymentSaveRequest request);

    void confirmPayment(Long vehicleId);

    PaymentResponse getPayment(Long vehicleId);
}
