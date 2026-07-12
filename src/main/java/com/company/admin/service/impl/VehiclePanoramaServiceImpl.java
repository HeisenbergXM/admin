package com.company.admin.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.dto.response.AllocationResponse;
import com.company.admin.dto.response.DispatchListResponse;
import com.company.admin.dto.response.InvoiceResponse;
import com.company.admin.dto.response.PaymentResponse;
import com.company.admin.dto.response.ProductionResponse;
import com.company.admin.dto.response.RegistrationResponse;
import com.company.admin.dto.response.TransportOrderDetailResponse;
import com.company.admin.dto.response.VehicleBasicInfo;
import com.company.admin.dto.response.VehiclePanoramaResponse;
import com.company.admin.dto.response.WaybillDealerResponse;
import com.company.admin.dto.response.WaybillResponse;
import com.company.admin.entity.DispatchList;
import com.company.admin.entity.TransportOrder;
import com.company.admin.entity.TransportOrderItem;
import com.company.admin.entity.Vehicle;
import com.company.admin.entity.VehAllocation;
import com.company.admin.entity.VehInvoice;
import com.company.admin.entity.VehPayment;
import com.company.admin.entity.VehProduction;
import com.company.admin.entity.VehRegistration;
import com.company.admin.entity.Waybill;
import com.company.admin.entity.WaybillDealer;
import com.company.admin.entity.WaybillDealerVin;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.OrderStatus;
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
import com.company.admin.service.VehiclePanoramaService;
import com.company.admin.service.BusinessStatusLabelService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehiclePanoramaServiceImpl implements VehiclePanoramaService {

    private final VehicleMapper vehicleMapper;
    private final VehProductionMapper vehProductionMapper;
    private final TransportOrderMapper transportOrderMapper;
    private final TransportOrderItemMapper transportOrderItemMapper;
    private final VehAllocationMapper vehAllocationMapper;
    private final VehInvoiceMapper vehInvoiceMapper;
    private final VehPaymentMapper vehPaymentMapper;
    private final DispatchListMapper dispatchListMapper;
    private final WaybillMapper waybillMapper;
    private final WaybillDealerMapper waybillDealerMapper;
    private final WaybillDealerVinMapper waybillDealerVinMapper;
    private final VehRegistrationMapper vehRegistrationMapper;
    private final BusinessStatusLabelService statusLabelService;

    @Override
    public VehiclePanoramaResponse getPanorama(String vin) {
        Vehicle vehicle = vehicleMapper.selectOne(new LambdaQueryWrapper<Vehicle>()
                .eq(Vehicle::getVin, vin == null ? null : vin.trim().toUpperCase())
                .eq(Vehicle::getDeleted, 0));
        if (vehicle == null) {
            throw new BusinessException(ErrorCode.VEHICLE_NOT_FOUND);
        }

        VehiclePanoramaResponse response = new VehiclePanoramaResponse();
        VehicleBasicInfo basicInfo = vehicleMapper.selectBasicInfoById(vehicle.getId());
        if (basicInfo == null) {
            basicInfo = new VehicleBasicInfo();
            basicInfo.setId(vehicle.getId());
            basicInfo.setVin(vehicle.getVin());
            basicInfo.setLifecycleStage(vehicle.getLifecycleStage());
        }
        response.setVehicle(basicInfo);
        fillProduction(vehicle.getId(), response);
        fillTransport(vehicle.getId(), basicInfo, response);
        fillAllocation(vehicle.getId(), basicInfo, response);
        fillInvoices(vehicle.getId(), vehicle.getVin(), response);
        fillPayment(vehicle.getId(), vehicle.getVin(), response);
        fillDispatch(vehicle.getId(), basicInfo, response);
        fillRegistration(vehicle.getId(), basicInfo, response);
        applyLabels(response);
        return response;
    }

    @Override
    public byte[] exportPanorama(String vin) {
        VehiclePanoramaResponse panorama = getPanorama(vin);
        List<ExportRow> rows = toExportRows(panorama);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        EasyExcel.write(outputStream, ExportRow.class).sheet("Panorama").doWrite(rows);
        return outputStream.toByteArray();
    }

    private void fillProduction(Long vehicleId, VehiclePanoramaResponse response) {
        VehProduction production = vehProductionMapper.selectOne(new LambdaQueryWrapper<VehProduction>()
                .eq(VehProduction::getVehicleId, vehicleId)
                .eq(VehProduction::getDeleted, 0));
        if (production == null) {
            return;
        }
        ProductionResponse dto = new ProductionResponse();
        BeanUtils.copyProperties(production, dto);
        response.setProduction(dto);
        addTimeline(response, LifecycleStage.PENDING_INBOUND.name(), "生产录入",
                production.getStageStatus(), production.getConfirmedBy(), production.getConfirmedAt());
    }

    private void fillTransport(Long vehicleId, VehicleBasicInfo basicInfo, VehiclePanoramaResponse response) {
        TransportOrderItem item = transportOrderItemMapper.selectOne(new LambdaQueryWrapper<TransportOrderItem>()
                .eq(TransportOrderItem::getVehicleId, vehicleId));
        if (item == null) {
            return;
        }
        TransportOrder order = transportOrderMapper.selectById(item.getTransportOrderId());
        if (order == null) {
            return;
        }
        TransportOrderDetailResponse dto = new TransportOrderDetailResponse();
        BeanUtils.copyProperties(order, dto);
        TransportOrderDetailResponse.Item itemDto = new TransportOrderDetailResponse.Item();
        itemDto.setId(item.getId());
        itemDto.setVehicleId(vehicleId);
        itemDto.setSaicBuyOffDate(item.getSaicBuyOffDate());
        itemDto.setVehicle(basicInfo);
        dto.setItems(List.of(itemDto));
        response.setTransportOrder(dto);
        addTimeline(response, LifecycleStage.PENDING_ALLOCATION.name(), "车厂到仓库",
                order.getOrderStatus(), order.getConfirmedBy(), order.getConfirmedAt());
    }

    private void fillAllocation(Long vehicleId, VehicleBasicInfo basicInfo, VehiclePanoramaResponse response) {
        VehAllocation allocation = vehAllocationMapper.selectOne(new LambdaQueryWrapper<VehAllocation>()
                .eq(VehAllocation::getVehicleId, vehicleId)
                .eq(VehAllocation::getDeleted, 0));
        if (allocation == null) {
            return;
        }
        AllocationResponse dto = new AllocationResponse();
        BeanUtils.copyProperties(allocation, dto);
        dto.setVin(basicInfo.getVin());
        dto.setDealerName(basicInfo.getDealerName());
        response.setAllocation(dto);
        addTimeline(response, LifecycleStage.PENDING_INVOICE.name(), "销售分配",
                allocation.getStageStatus(), allocation.getConfirmedBy(), allocation.getConfirmedAt());
    }

    private void fillInvoices(Long vehicleId, String vin, VehiclePanoramaResponse response) {
        List<VehInvoice> invoices = vehInvoiceMapper.selectList(new LambdaQueryWrapper<VehInvoice>()
                .eq(VehInvoice::getVehicleId, vehicleId)
                .eq(VehInvoice::getDeleted, 0)
                .orderByAsc(VehInvoice::getInvoiceSeq));
        response.setInvoices(invoices.stream().map(invoice -> {
            InvoiceResponse dto = new InvoiceResponse();
            BeanUtils.copyProperties(invoice, dto);
            dto.setVin(vin);
            return dto;
        }).collect(java.util.stream.Collectors.toList()));
        invoices.stream()
                .filter(invoice -> StageStatus.CONFIRMED.name().equals(invoice.getStageStatus()))
                .findFirst()
                .ifPresent(invoice -> addTimeline(response, LifecycleStage.PENDING_PAYMENT.name(), "发票确认",
                        invoice.getStageStatus(), invoice.getConfirmedBy(), invoice.getConfirmedAt()));
    }

    private void fillPayment(Long vehicleId, String vin, VehiclePanoramaResponse response) {
        VehPayment payment = vehPaymentMapper.selectOne(new LambdaQueryWrapper<VehPayment>()
                .eq(VehPayment::getVehicleId, vehicleId)
                .eq(VehPayment::getDeleted, 0));
        if (payment == null) {
            return;
        }
        PaymentResponse dto = new PaymentResponse();
        BeanUtils.copyProperties(payment, dto);
        dto.setVin(vin);
        dto.setHasFormalInvoice(hasLatestFormalInvoice(response.getInvoices()));
        response.setPayment(dto);
        addTimeline(response, LifecycleStage.PENDING_DELIVERY.name(), "收款确认",
                payment.getStageStatus(), payment.getConfirmedBy(), payment.getConfirmedAt());
    }

    private void fillDispatch(Long vehicleId, VehicleBasicInfo basicInfo, VehiclePanoramaResponse response) {
        WaybillDealerVin vin = waybillDealerVinMapper.selectOne(new LambdaQueryWrapper<WaybillDealerVin>()
                .eq(WaybillDealerVin::getVehicleId, vehicleId));
        if (vin == null) {
            return;
        }
        WaybillDealer dealer = waybillDealerMapper.selectById(vin.getWaybillDealerId());
        if (dealer == null) {
            return;
        }
        Waybill waybill = waybillMapper.selectById(dealer.getWaybillId());
        if (waybill == null) {
            return;
        }
        DispatchList dispatchList = dispatchListMapper.selectById(waybill.getDispatchListId());
        if (dispatchList == null) {
            return;
        }

        DispatchListResponse dispatchResponse = new DispatchListResponse();
        BeanUtils.copyProperties(dispatchList, dispatchResponse);
        WaybillResponse waybillResponse = new WaybillResponse();
        BeanUtils.copyProperties(waybill, waybillResponse);
        WaybillDealerResponse dealerResponse = new WaybillDealerResponse();
        BeanUtils.copyProperties(dealer, dealerResponse);
        dealerResponse.setVins(List.of(basicInfo));
        waybillResponse.setDealers(List.of(dealerResponse));
        dispatchResponse.setWaybills(List.of(waybillResponse));
        response.setDispatch(dispatchResponse);
        addTimeline(response, LifecycleStage.PENDING_REGISTRATION.name(), "配送签收",
                dealer.getRowStatus(), dealer.getConfirmedBy(), dealer.getConfirmedAt());
    }

    private void fillRegistration(Long vehicleId, VehicleBasicInfo basicInfo, VehiclePanoramaResponse response) {
        VehRegistration registration = vehRegistrationMapper.selectOne(new LambdaQueryWrapper<VehRegistration>()
                .eq(VehRegistration::getVehicleId, vehicleId)
                .eq(VehRegistration::getDeleted, 0));
        if (registration == null) {
            return;
        }
        RegistrationResponse dto = new RegistrationResponse();
        BeanUtils.copyProperties(registration, dto);
        dto.setVin(basicInfo.getVin());
        dto.setDealerId(basicInfo.getDealerId());
        dto.setDealerName(basicInfo.getDealerName());
        response.setRegistration(dto);
        addTimeline(response, LifecycleStage.COMPLETED.name(), "车辆上牌",
                registration.getStageStatus(), registration.getConfirmedBy(), registration.getConfirmedAt());
    }

    private void addTimeline(VehiclePanoramaResponse response, String stage, String name,
                             String status, String confirmedBy, java.time.LocalDateTime confirmedAt) {
        if (!StageStatus.CONFIRMED.name().equals(status) && !OrderStatus.CONFIRMED.name().equals(status)) {
            return;
        }
        VehiclePanoramaResponse.TimelineNode node = new VehiclePanoramaResponse.TimelineNode();
        node.setStage(stage);
        node.setName(name);
        node.setConfirmedBy(confirmedBy);
        node.setConfirmedAt(confirmedAt);
        response.getTimeline().add(node);
    }

    private boolean hasLatestFormalInvoice(List<InvoiceResponse> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            return false;
        }
        InvoiceResponse latest = invoices.get(invoices.size() - 1);
        return "INVOICED".equals(latest.getInvoiceType())
                && StageStatus.CONFIRMED.name().equals(latest.getStageStatus());
    }

    private List<ExportRow> toExportRows(VehiclePanoramaResponse panorama) {
        List<ExportRow> rows = new ArrayList<>();
        addRow(rows, "vehicle", "vin", panorama.getVehicle().getVin());
        addRow(rows, "vehicle", "lifecycleStage", panorama.getVehicle().getLifecycleStageLabel());
        if (panorama.getProduction() != null) {
            addRow(rows, "production", "stageStatus", panorama.getProduction().getStageStatusLabel());
        }
        if (panorama.getAllocation() != null) {
            addRow(rows, "allocation", "dealerId", String.valueOf(panorama.getAllocation().getDealerId()));
        }
        if (panorama.getPayment() != null) {
            addRow(rows, "payment", "paymentStatus", panorama.getPayment().getPaymentStatusLabel());
        }
        if (panorama.getRegistration() != null) {
            addRow(rows, "registration", "registrationDate", String.valueOf(panorama.getRegistration().getRegistrationDate()));
        }
        for (InvoiceResponse invoice : panorama.getInvoices()) {
            addRow(rows, "invoice-" + invoice.getInvoiceSeq(), "invoiceType", invoice.getInvoiceTypeLabel());
            addRow(rows, "invoice-" + invoice.getInvoiceSeq(), "invoiceNo", invoice.getInvoiceNo());
        }
        return rows;
    }

    private void addRow(List<ExportRow> rows, String section, String field, String value) {
        ExportRow row = new ExportRow();
        row.setSection(section);
        row.setField(field);
        row.setValue(value);
        rows.add(row);
    }

    private void applyLabels(VehiclePanoramaResponse response) {
        applyLabels(response.getVehicle());
        if (response.getProduction() != null) {
            response.getProduction().setStageStatusLabel(
                    statusLabelService.stageStatusLabel(response.getProduction().getStageStatus()));
        }
        if (response.getTransportOrder() != null) {
            response.getTransportOrder().setOrderStatusLabel(
                    statusLabelService.orderStatusLabel(response.getTransportOrder().getOrderStatus()));
            if (response.getTransportOrder().getItems() != null) {
                response.getTransportOrder().getItems().forEach(item -> applyLabels(item.getVehicle()));
            }
        }
        if (response.getAllocation() != null) {
            response.getAllocation().setStageStatusLabel(
                    statusLabelService.stageStatusLabel(response.getAllocation().getStageStatus()));
            applyLabels(response.getAllocation().getLifecycleStage(), response.getAllocation());
        }
        response.getInvoices().forEach(invoice -> {
            invoice.setStageStatusLabel(statusLabelService.stageStatusLabel(invoice.getStageStatus()));
            invoice.setInvoiceTypeLabel(statusLabelService.dictLabel("invoice_status", invoice.getInvoiceType()));
        });
        if (response.getPayment() != null) {
            response.getPayment().setLifecycleStageLabel(
                    statusLabelService.lifecycleStageLabel(response.getPayment().getLifecycleStage()));
            response.getPayment().setStageStatusLabel(
                    statusLabelService.stageStatusLabel(response.getPayment().getStageStatus()));
            response.getPayment().setPaymentStatusLabel(
                    statusLabelService.dictLabel("payment_status", response.getPayment().getPaymentStatus()));
        }
        if (response.getDispatch() != null) {
            response.getDispatch().setListStatusLabel(
                    statusLabelService.stageStatusLabel(response.getDispatch().getListStatus()));
            response.getDispatch().getWaybills().forEach(waybill -> waybill.getDealers().forEach(dealer -> {
                dealer.setDeliveryStatusLabel(
                        statusLabelService.dictLabel("delivery_status", dealer.getDeliveryStatus()));
                dealer.setRowStatusLabel(statusLabelService.stageStatusLabel(dealer.getRowStatus()));
                dealer.getVins().forEach(this::applyLabels);
            }));
        }
        if (response.getRegistration() != null) {
            response.getRegistration().setLifecycleStageLabel(
                    statusLabelService.lifecycleStageLabel(response.getRegistration().getLifecycleStage()));
            response.getRegistration().setStageStatusLabel(
                    statusLabelService.stageStatusLabel(response.getRegistration().getStageStatus()));
            response.getRegistration().setDrosstechStatusLabel(
                    statusLabelService.dictLabel("drosstech_status", response.getRegistration().getDrosstechStatus()));
        }
        response.getTimeline().forEach(node -> node.setStageLabel(
                statusLabelService.lifecycleStageLabel(node.getStage())));
    }

    private void applyLabels(VehicleBasicInfo info) {
        if (info != null) {
            info.setLifecycleStageLabel(statusLabelService.lifecycleStageLabel(info.getLifecycleStage()));
        }
    }

    private void applyLabels(String lifecycleStage, AllocationResponse response) {
        response.setLifecycleStageLabel(statusLabelService.lifecycleStageLabel(lifecycleStage));
        response.setSalesStatusLabel(statusLabelService.dictLabel("sales_status", response.getSalesStatus()));
    }

    @Data
    public static class ExportRow {
        @ExcelProperty("Section")
        private String section;
        @ExcelProperty("Field")
        private String field;
        @ExcelProperty("Value")
        private String value;
    }
}
