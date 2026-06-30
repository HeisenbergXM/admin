package com.company.admin.service.impl;

import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.dto.request.InvoiceConvertRequest;
import com.company.admin.dto.request.InvoiceCreateRequest;
import com.company.admin.entity.VehInvoice;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.StageStatus;
import com.company.admin.mapper.VehInvoiceMapper;
import com.company.admin.service.LifecycleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehInvoiceServiceImplTest {

    @Mock
    private VehInvoiceMapper vehInvoiceMapper;

    @Mock
    private LifecycleService lifecycleService;

    @InjectMocks
    private VehInvoiceServiceImpl service;

    @Test
    void createInvoiceConfirmsFirstInvoiceAndAdvancesToPayment() {
        when(vehInvoiceMapper.selectCount(any())).thenReturn(0L);
        service.createInvoice(99L, createRequest("INVOICED"));

        verify(lifecycleService).assertStage(99L, LifecycleStage.PENDING_INVOICE);
        verify(lifecycleService).advanceStage(99L, LifecycleStage.PENDING_INVOICE, LifecycleStage.PENDING_PAYMENT);
        ArgumentCaptor<VehInvoice> captor = ArgumentCaptor.forClass(VehInvoice.class);
        verify(vehInvoiceMapper).insert(captor.capture());
        assertEquals(99L, captor.getValue().getVehicleId());
        assertEquals(1, captor.getValue().getInvoiceSeq());
        assertEquals("INVOICED", captor.getValue().getInvoiceType());
        assertEquals(StageStatus.CONFIRMED.name(), captor.getValue().getStageStatus());
        assertNotNull(captor.getValue().getConfirmedAt());
    }

    @Test
    void convertRejectsVehicleWhoseLatestInvoiceIsAlreadyFormal() {
        when(vehInvoiceMapper.selectList(any())).thenReturn(List.of(invoice(1, "INVOICED")));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.convertProforma(99L, convertRequest()));

        assertEquals(ErrorCode.INVOICE_NOT_FORMAL.getCode(), exception.getCode());
    }

    @Test
    void convertCreatesSecondFormalInvoiceForProformaVehicle() {
        when(vehInvoiceMapper.selectList(any())).thenReturn(List.of(invoice(1, "PROFORMA_INVOICED")));

        service.convertProforma(99L, convertRequest());

        verify(lifecycleService).assertStage(99L, LifecycleStage.PENDING_PAYMENT);
        ArgumentCaptor<VehInvoice> captor = ArgumentCaptor.forClass(VehInvoice.class);
        verify(vehInvoiceMapper).insert(captor.capture());
        assertEquals(2, captor.getValue().getInvoiceSeq());
        assertEquals("INVOICED", captor.getValue().getInvoiceType());
        assertEquals(StageStatus.CONFIRMED.name(), captor.getValue().getStageStatus());
    }

    private InvoiceCreateRequest createRequest(String invoiceType) {
        InvoiceCreateRequest request = new InvoiceCreateRequest();
        request.setInvoiceType(invoiceType);
        request.setInvoiceNo("INV-001");
        request.setInvoiceDate(LocalDate.of(2026, 7, 3));
        request.setRemark("remark");
        return request;
    }

    private InvoiceConvertRequest convertRequest() {
        InvoiceConvertRequest request = new InvoiceConvertRequest();
        request.setInvoiceNo("INV-002");
        request.setInvoiceDate(LocalDate.of(2026, 7, 4));
        request.setRemark("converted");
        return request;
    }

    private VehInvoice invoice(Integer seq, String invoiceType) {
        VehInvoice invoice = new VehInvoice();
        invoice.setId(seq.longValue());
        invoice.setVehicleId(99L);
        invoice.setInvoiceSeq(seq);
        invoice.setInvoiceType(invoiceType);
        invoice.setStageStatus(StageStatus.CONFIRMED.name());
        return invoice;
    }
}
