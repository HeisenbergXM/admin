package com.company.admin.service.impl;

import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.dto.response.DeliveryResponse;
import com.company.admin.dto.response.InboundResponse;
import com.company.admin.dto.response.VehicleBasicInfo;
import com.company.admin.dto.response.VehiclePanoramaResponse;
import com.company.admin.entity.Vehicle;
import com.company.admin.entity.VehInvoice;
import com.company.admin.entity.VehPayment;
import com.company.admin.entity.VehProduction;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.StageStatus;
import com.company.admin.mapper.VehAllocationMapper;
import com.company.admin.mapper.VehDeliveryMapper;
import com.company.admin.mapper.VehInvoiceMapper;
import com.company.admin.mapper.VehInboundMapper;
import com.company.admin.mapper.VehPaymentMapper;
import com.company.admin.mapper.VehProductionMapper;
import com.company.admin.mapper.VehRegistrationMapper;
import com.company.admin.mapper.VehicleMapper;
import com.company.admin.service.BusinessStatusLabelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehiclePanoramaServiceImplTest {

    @Mock private VehicleMapper vehicleMapper;
    @Mock private VehProductionMapper vehProductionMapper;
    @Mock private VehInboundMapper vehInboundMapper;
    @Mock private VehDeliveryMapper vehDeliveryMapper;
    @Mock private VehAllocationMapper vehAllocationMapper;
    @Mock private VehInvoiceMapper vehInvoiceMapper;
    @Mock private VehPaymentMapper vehPaymentMapper;
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
    void getPanoramaUsesVinLevelInboundAndDelivery() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(99L);
        vehicle.setVin("LSJW56U95RG000001");
        vehicle.setLifecycleStage(LifecycleStage.PENDING_REGISTRATION.name());
        VehicleBasicInfo basic = new VehicleBasicInfo();
        basic.setId(99L);
        basic.setVin(vehicle.getVin());
        basic.setLifecycleStage(vehicle.getLifecycleStage());
        InboundResponse inbound = new InboundResponse();
        inbound.setId(61L);
        inbound.setVehicleId(99L);
        inbound.setLifecycleStage(LifecycleStage.PENDING_ALLOCATION.name());
        inbound.setStageStatus("CONFIRMED");
        inbound.setConfirmedAt(LocalDateTime.of(2026, 7, 14, 10, 0));
        DeliveryResponse delivery = new DeliveryResponse();
        delivery.setId(62L);
        delivery.setVehicleId(99L);
        delivery.setLifecycleStage(LifecycleStage.PENDING_REGISTRATION.name());
        delivery.setStageStatus("CONFIRMED");
        delivery.setDeliveryStatus("DELIVERED");
        delivery.setConfirmedAt(LocalDateTime.of(2026, 7, 16, 10, 0));
        when(vehicleMapper.selectOne(any())).thenReturn(vehicle);
        when(vehicleMapper.selectBasicInfoById(99L)).thenReturn(basic);
        when(vehInboundMapper.selectInboundByVehicleId(99L)).thenReturn(inbound);
        when(vehDeliveryMapper.selectDeliveryByVehicleId(99L)).thenReturn(delivery);
        when(vehInvoiceMapper.selectList(any())).thenReturn(List.of());
        when(statusLabelService.stageStatusLabel("CONFIRMED")).thenReturn("已确认");
        when(statusLabelService.lifecycleStageLabel(LifecycleStage.PENDING_ALLOCATION.name()))
                .thenReturn("待分配");
        when(statusLabelService.lifecycleStageLabel(LifecycleStage.PENDING_REGISTRATION.name()))
                .thenReturn("待上牌");
        when(statusLabelService.dictLabel("delivery_status", "DELIVERED")).thenReturn("已送达");

        VehiclePanoramaResponse response = service.getPanorama(vehicle.getVin());

        assertEquals(inbound.getVehicleId(), response.getInbound().getVehicleId());
        assertEquals("已确认", response.getInbound().getStageStatusLabel());
        assertEquals("待分配", response.getInbound().getLifecycleStageLabel());
        assertEquals("DELIVERED", response.getDelivery().getDeliveryStatus());
        assertEquals("已送达", response.getDelivery().getDeliveryStatusLabel());
        assertEquals("待上牌", response.getDelivery().getLifecycleStageLabel());
        assertEquals(List.of("PENDING_ALLOCATION", "PENDING_REGISTRATION"),
                response.getTimeline().stream().map(VehiclePanoramaResponse.TimelineNode::getStage).toList());
        assertNull(response.getTransportOrder());
        assertNull(response.getDispatch());
    }

    @Test
    void getPanoramaOmitsUncreatedVinLevelLogistics() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(99L);
        vehicle.setVin("LSJW56U95RG000001");
        VehicleBasicInfo basic = new VehicleBasicInfo();
        basic.setId(99L);
        basic.setVin(vehicle.getVin());
        InboundResponse inbound = new InboundResponse();
        DeliveryResponse delivery = new DeliveryResponse();
        when(vehicleMapper.selectOne(any())).thenReturn(vehicle);
        when(vehicleMapper.selectBasicInfoById(99L)).thenReturn(basic);
        when(vehInboundMapper.selectInboundByVehicleId(99L)).thenReturn(inbound);
        when(vehDeliveryMapper.selectDeliveryByVehicleId(99L)).thenReturn(delivery);
        when(vehInvoiceMapper.selectList(any())).thenReturn(List.of());

        VehiclePanoramaResponse response = service.getPanorama(vehicle.getVin());

        assertNull(response.getInbound());
        assertNull(response.getDelivery());
        assertEquals(List.of(), response.getTimeline());
    }

    @Test
    void exportRowsContainVinLevelLogisticsSections() {
        VehiclePanoramaResponse panorama = new VehiclePanoramaResponse();
        VehicleBasicInfo vehicle = new VehicleBasicInfo();
        vehicle.setVin("LSJW56U95RG000001");
        panorama.setVehicle(vehicle);
        InboundResponse inbound = new InboundResponse();
        inbound.setId(61L);
        inbound.setSaicBuyOffDate(LocalDate.of(2026, 7, 10));
        inbound.setDateToStorageYard(LocalDate.of(2026, 7, 11));
        inbound.setConfirmedBy("inbound-user");
        inbound.setConfirmedAt(LocalDateTime.of(2026, 7, 14, 10, 15));
        panorama.setInbound(inbound);
        DeliveryResponse delivery = new DeliveryResponse();
        delivery.setId(62L);
        delivery.setEtdToDealer(LocalDate.of(2026, 7, 15));
        delivery.setEtaToDealer(LocalDate.of(2026, 7, 16));
        delivery.setTrollyType("FLATBED");
        delivery.setFullyLoad(true);
        delivery.setReceivedDate(LocalDate.of(2026, 7, 17));
        delivery.setDeliveryStatusLabel("已交付");
        delivery.setConfirmedBy("delivery-user");
        delivery.setConfirmedAt(LocalDateTime.of(2026, 7, 16, 10, 30));
        panorama.setDelivery(delivery);

        List<VehiclePanoramaServiceImpl.ExportRow> rows = service.toExportRows(panorama);

        List<VehiclePanoramaServiceImpl.ExportRow> inboundRows = rows.stream()
                .filter(row -> "inbound".equals(row.getSection()))
                .toList();
        assertEquals(List.of("inbound", "inbound", "inbound", "inbound"),
                inboundRows.stream().map(VehiclePanoramaServiceImpl.ExportRow::getSection).toList());
        assertEquals(List.of("saicBuyOffDate", "dateToStorageYard", "confirmedBy", "confirmedAt"),
                inboundRows.stream().map(VehiclePanoramaServiceImpl.ExportRow::getField).toList());
        assertEquals(List.of("2026-07-10", "2026-07-11", "inbound-user", "2026-07-14T10:15"),
                inboundRows.stream().map(VehiclePanoramaServiceImpl.ExportRow::getValue).toList());

        List<VehiclePanoramaServiceImpl.ExportRow> deliveryRows = rows.stream()
                .filter(row -> "delivery".equals(row.getSection()))
                .toList();
        assertEquals(List.of("delivery", "delivery", "delivery", "delivery",
                        "delivery", "delivery", "delivery", "delivery"),
                deliveryRows.stream().map(VehiclePanoramaServiceImpl.ExportRow::getSection).toList());
        assertEquals(List.of("etdToDealer", "etaToDealer", "trollyType", "fullyLoad",
                        "receivedDate", "deliveryStatus", "confirmedBy", "confirmedAt"),
                deliveryRows.stream().map(VehiclePanoramaServiceImpl.ExportRow::getField).toList());
        assertEquals(List.of("2026-07-15", "2026-07-16", "FLATBED", "true",
                        "2026-07-17", "已交付", "delivery-user", "2026-07-16T10:30"),
                deliveryRows.stream().map(VehiclePanoramaServiceImpl.ExportRow::getValue).toList());
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
