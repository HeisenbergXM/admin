package com.company.admin.service.impl;

import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.dto.request.PaymentSaveRequest;
import com.company.admin.dto.response.PaymentResponse;
import com.company.admin.dto.response.VehicleBasicInfo;
import com.company.admin.entity.VehPayment;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.StageStatus;
import com.company.admin.mapper.VehPaymentMapper;
import com.company.admin.service.LifecycleService;
import com.company.admin.service.VehicleBasicService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehPaymentServiceImplTest {

    @Mock
    private VehPaymentMapper vehPaymentMapper;

    @Mock
    private LifecycleService lifecycleService;

    @Mock
    private VehicleBasicService vehicleBasicService;

    @InjectMocks
    private VehPaymentServiceImpl service;

    @Test
    void createPaymentChecksPendingPaymentAndStoresDraft() {
        doAnswer(invocation -> {
            VehPayment payment = invocation.getArgument(0);
            payment.setId(12L);
            return 1;
        }).when(vehPaymentMapper).insert(any(VehPayment.class));

        Long id = service.createPayment(99L, saveRequest());

        assertEquals(12L, id);
        verify(lifecycleService).assertStage(99L, LifecycleStage.PENDING_PAYMENT);
        ArgumentCaptor<VehPayment> captor = ArgumentCaptor.forClass(VehPayment.class);
        verify(vehPaymentMapper).insert(captor.capture());
        assertEquals(StageStatus.DRAFT.name(), captor.getValue().getStageStatus());
        assertEquals("PAID", captor.getValue().getPaymentStatus());
    }

    @Test
    void confirmPaymentRejectsVehicleWithoutFormalInvoice() {
        when(vehPaymentMapper.selectOne(any())).thenReturn(draftPayment());
        when(lifecycleService.hasValidFormalInvoice(99L)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.confirmPayment(99L));

        assertEquals(ErrorCode.INVOICE_NOT_FORMAL.getCode(), exception.getCode());
    }

    @Test
    void confirmPaymentLocksAndAdvancesLifecycle() {
        VehPayment payment = draftPayment();
        when(vehPaymentMapper.selectOne(any())).thenReturn(payment);
        when(lifecycleService.hasValidFormalInvoice(99L)).thenReturn(true);
        doAnswer(invocation -> {
            Runnable lockAction = invocation.getArgument(4);
            lockAction.run();
            return null;
        }).when(lifecycleService).confirmAndAdvance(
                eq(99L),
                eq(StageStatus.DRAFT.name()),
                eq(LifecycleStage.PENDING_PAYMENT),
                eq(LifecycleStage.PENDING_DELIVERY),
                any(Runnable.class));

        service.confirmPayment(99L);

        ArgumentCaptor<VehPayment> captor = ArgumentCaptor.forClass(VehPayment.class);
        verify(vehPaymentMapper).updateById(captor.capture());
        assertEquals(StageStatus.CONFIRMED.name(), captor.getValue().getStageStatus());
        assertNotNull(captor.getValue().getConfirmedAt());
    }

    @Test
    void getPaymentIncludesVehicleLifecycleStage() {
        when(vehPaymentMapper.selectOne(any())).thenReturn(draftPayment());
        when(lifecycleService.hasValidFormalInvoice(99L)).thenReturn(true);
        VehicleBasicInfo basicInfo = new VehicleBasicInfo();
        basicInfo.setVin("VIN00000000000099");
        basicInfo.setLifecycleStage(LifecycleStage.PENDING_PAYMENT.name());
        when(vehicleBasicService.getBasicInfo(99L)).thenReturn(basicInfo);

        PaymentResponse response = service.getPayment(99L);

        assertEquals("VIN00000000000099", response.getVin());
        assertEquals(LifecycleStage.PENDING_PAYMENT.name(), response.getLifecycleStage());
    }

    private PaymentSaveRequest saveRequest() {
        PaymentSaveRequest request = new PaymentSaveRequest();
        request.setPaymentDate(LocalDate.of(2026, 7, 5));
        request.setCreditFullPaymentDate(LocalDate.of(2026, 7, 6));
        request.setPaymentStatus("PAID");
        request.setRemark5("remark");
        return request;
    }

    private VehPayment draftPayment() {
        VehPayment payment = new VehPayment();
        payment.setId(12L);
        payment.setVehicleId(99L);
        payment.setStageStatus(StageStatus.DRAFT.name());
        return payment;
    }
}
