package com.company.admin.service.impl;

import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.dto.response.VehicleBasicInfo;
import com.company.admin.dto.response.VehiclePanoramaResponse;
import com.company.admin.entity.Vehicle;
import com.company.admin.entity.VehInvoice;
import com.company.admin.entity.VehPayment;
import com.company.admin.entity.VehProduction;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.StageStatus;
import com.company.admin.mapper.DispatchListMapper;
import com.company.admin.mapper.TransportOrderItemMapper;
import com.company.admin.mapper.TransportOrderMapper;
import com.company.admin.mapper.VehAllocationMapper;
import com.company.admin.mapper.VehInvoiceMapper;
import com.company.admin.mapper.VehPaymentMapper;
import com.company.admin.mapper.VehProductionMapper;
import com.company.admin.mapper.VehRegistrationMapper;
import com.company.admin.mapper.VehicleMapper;
import com.company.admin.mapper.WaybillDealerMapper;
import com.company.admin.mapper.WaybillDealerVinMapper;
import com.company.admin.mapper.WaybillMapper;
import com.company.admin.service.BusinessStatusLabelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehiclePanoramaServiceImplTest {

    @Mock private VehicleMapper vehicleMapper;
    @Mock private VehProductionMapper vehProductionMapper;
    @Mock private TransportOrderMapper transportOrderMapper;
    @Mock private TransportOrderItemMapper transportOrderItemMapper;
    @Mock private VehAllocationMapper vehAllocationMapper;
    @Mock private VehInvoiceMapper vehInvoiceMapper;
    @Mock private VehPaymentMapper vehPaymentMapper;
    @Mock private DispatchListMapper dispatchListMapper;
    @Mock private WaybillMapper waybillMapper;
    @Mock private WaybillDealerMapper waybillDealerMapper;
    @Mock private WaybillDealerVinMapper waybillDealerVinMapper;
    @Mock private VehRegistrationMapper vehRegistrationMapper;
    @Mock private BusinessStatusLabelService statusLabelService;

    @InjectMocks
    private VehiclePanoramaServiceImpl service;

    @Test
    void getPanoramaRejectsMissingVin() {
        when(vehicleMapper.selectOne(any())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.getPanorama("UNKNOWN"));

        assertEquals(ErrorCode.VEHICLE_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void getPanoramaReturnsVehicleProductionAndInvoiceHistory() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(99L);
        vehicle.setVin("LSJW56U95RG000001");
        vehicle.setLifecycleStage(LifecycleStage.PENDING_PAYMENT.name());
        VehicleBasicInfo basicInfo = new VehicleBasicInfo();
        basicInfo.setId(99L);
        basicInfo.setVin(vehicle.getVin());
        basicInfo.setLifecycleStage(vehicle.getLifecycleStage());
        VehProduction production = new VehProduction();
        production.setVehicleId(99L);
        production.setStageStatus(StageStatus.CONFIRMED.name());
        production.setConfirmedAt(LocalDateTime.of(2026, 7, 1, 9, 0));
        when(vehicleMapper.selectOne(any())).thenReturn(vehicle);
        when(vehicleMapper.selectBasicInfoById(99L)).thenReturn(basicInfo);
        when(vehProductionMapper.selectOne(any())).thenReturn(production);
        when(vehInvoiceMapper.selectList(any())).thenReturn(List.of(invoice(1), invoice(2)));
        when(statusLabelService.lifecycleStageLabel("PENDING_PAYMENT")).thenReturn("待收款");
        when(statusLabelService.lifecycleStageLabel("PENDING_INBOUND")).thenReturn("待入库");

        VehiclePanoramaResponse response = service.getPanorama(vehicle.getVin());

        assertEquals("LSJW56U95RG000001", response.getVehicle().getVin());
        assertEquals(2, response.getInvoices().size());
        assertEquals("PENDING_INBOUND", response.getTimeline().get(0).getStage());
        assertEquals("待入库", response.getTimeline().get(0).getStageLabel());
    }

    @Test
    void getPanoramaDoesNotTreatDraftFormalInvoiceAsEffective() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(99L);
        vehicle.setVin("LSJW56U95RG000001");
        vehicle.setLifecycleStage(LifecycleStage.PENDING_PAYMENT.name());
        VehicleBasicInfo basicInfo = new VehicleBasicInfo();
        basicInfo.setId(99L);
        basicInfo.setVin(vehicle.getVin());
        VehPayment payment = new VehPayment();
        payment.setVehicleId(99L);
        payment.setStageStatus(StageStatus.DRAFT.name());

        when(vehicleMapper.selectOne(any())).thenReturn(vehicle);
        when(vehicleMapper.selectBasicInfoById(99L)).thenReturn(basicInfo);
        when(vehInvoiceMapper.selectList(any())).thenReturn(List.of(
                invoice(1, "PROFORMA_INVOICED", StageStatus.CONFIRMED.name()),
                invoice(2, "INVOICED", StageStatus.DRAFT.name())));
        when(vehPaymentMapper.selectOne(any())).thenReturn(payment);

        VehiclePanoramaResponse response = service.getPanorama(vehicle.getVin());

        assertEquals(Boolean.FALSE, response.getPayment().getHasFormalInvoice());
    }

    private VehInvoice invoice(Integer seq) {
        return invoice(seq, "INVOICED", StageStatus.CONFIRMED.name());
    }

    private VehInvoice invoice(Integer seq, String type, String status) {
        VehInvoice invoice = new VehInvoice();
        invoice.setVehicleId(99L);
        invoice.setInvoiceSeq(seq);
        invoice.setInvoiceType(type);
        invoice.setStageStatus(status);
        invoice.setConfirmedAt(LocalDateTime.of(2026, 7, seq, 10, 0));
        return invoice;
    }
}
