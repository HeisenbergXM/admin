package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.export.VehicleCorrectionExcelExporter;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest.AllocationCorrection;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest.DeliveryCorrection;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest.InboundCorrection;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest.InvoiceCorrection;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest.PaymentCorrection;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest.ProductionCorrection;
import com.company.admin.dto.request.VehicleCorrectionUpdateRequest.RegistrationCorrection;
import com.company.admin.dto.request.VehicleQueryRequest;
import com.company.admin.dto.response.VehicleCorrectionListResponse;
import com.company.admin.dto.response.VehiclePanoramaResponse;
import com.company.admin.entity.Dealer;
import com.company.admin.entity.ExteriorColor;
import com.company.admin.entity.InteriorColor;
import com.company.admin.entity.VehAllocation;
import com.company.admin.entity.VehDelivery;
import com.company.admin.entity.VehInbound;
import com.company.admin.entity.VehInvoice;
import com.company.admin.entity.VehPayment;
import com.company.admin.entity.VehProduction;
import com.company.admin.entity.VehRegistration;
import com.company.admin.entity.Vehicle;
import com.company.admin.entity.VehicleModel;
import com.company.admin.mapper.DealerMapper;
import com.company.admin.mapper.ExteriorColorMapper;
import com.company.admin.mapper.InteriorColorMapper;
import com.company.admin.mapper.VehAllocationMapper;
import com.company.admin.mapper.VehDeliveryMapper;
import com.company.admin.mapper.VehInboundMapper;
import com.company.admin.mapper.VehInvoiceMapper;
import com.company.admin.mapper.VehPaymentMapper;
import com.company.admin.mapper.VehProductionMapper;
import com.company.admin.mapper.VehRegistrationMapper;
import com.company.admin.mapper.VehicleMapper;
import com.company.admin.mapper.VehicleModelMapper;
import com.company.admin.service.BusinessStatusLabelService;
import com.company.admin.service.VehicleCorrectionAuditService;
import com.company.admin.service.VehicleCorrectionAuditService.CorrectionDiff;
import com.company.admin.service.VehicleCorrectionService;
import com.company.admin.service.VehiclePanoramaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VehicleCorrectionServiceImpl implements VehicleCorrectionService {

    private final VehicleMapper vehicleMapper;
    private final VehiclePanoramaService vehiclePanoramaService;
    private final BusinessStatusLabelService statusLabelService;
    private final VehProductionMapper vehProductionMapper;
    private final VehInboundMapper vehInboundMapper;
    private final VehAllocationMapper vehAllocationMapper;
    private final VehInvoiceMapper vehInvoiceMapper;
    private final VehPaymentMapper vehPaymentMapper;
    private final VehDeliveryMapper vehDeliveryMapper;
    private final VehRegistrationMapper vehRegistrationMapper;
    private final VehicleModelMapper vehicleModelMapper;
    private final ExteriorColorMapper exteriorColorMapper;
    private final InteriorColorMapper interiorColorMapper;
    private final DealerMapper dealerMapper;
    private final VehicleCorrectionAuditService auditService;
    private final VehicleCorrectionExcelExporter vehicleCorrectionExcelExporter;

    @Override
    public PageResult<VehicleCorrectionListResponse> pageCorrections(VehicleQueryRequest request) {
        Page<VehicleCorrectionListResponse> page = vehicleMapper.selectVehicleCorrectionMasterSheetPage(
                new Page<>(request.getPageNum(), request.getPageSize()), request);
        long firstNo = ((long) request.getPageNum() - 1L) * request.getPageSize() + 1L;
        prepareRows(page.getRecords(), firstNo);
        return new PageResult<>(page.getRecords(), page.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public byte[] exportCorrections(VehicleQueryRequest request) {
        List<VehicleCorrectionListResponse> rows = vehicleMapper.selectVehicleCorrections(request);
        prepareRows(rows, 1L);
        return vehicleCorrectionExcelExporter.export(rows);
    }

    @Override
    public VehiclePanoramaResponse getCorrection(Long vehicleId) {
        Vehicle vehicle = vehicleMapper.selectById(vehicleId);
        if (vehicle == null || Integer.valueOf(1).equals(vehicle.getDeleted())) {
            throw new BusinessException(ErrorCode.VEHICLE_NOT_FOUND);
        }
        return vehiclePanoramaService.getPanorama(vehicle.getVin());
    }

    @Override
    @Transactional
    public void updateCorrection(Long vehicleId, VehicleCorrectionUpdateRequest request) {
        if (vehicleId == null || request == null) {
            throw badRequest("车辆 ID 和修订内容不能为空");
        }
        if (!hasCorrectionSection(request)) {
            throw badRequest("修订内容不能为空");
        }
        Vehicle vehicle = vehicleMapper.selectByIdForUpdate(vehicleId);
        if (vehicle == null) {
            throw new BusinessException(ErrorCode.VEHICLE_NOT_FOUND);
        }
        validateInvoiceIds(request);

        CorrectionDiff diff = new CorrectionDiff(vehicleId, vehicle.getVin());
        if (request.getProduction() != null) {
            updateProduction(vehicleId, request.getProduction(), diff);
        }
        if (request.getInbound() != null) {
            updateInbound(vehicleId, request.getInbound(), diff);
        }
        if (request.getAllocation() != null) {
            updateAllocation(vehicleId, request.getAllocation(), diff);
        }
        if (request.getInvoices() != null) {
            for (InvoiceCorrection invoice : request.getInvoices()) {
                updateInvoice(vehicleId, invoice, diff);
            }
        }
        if (request.getPayment() != null) {
            updatePayment(vehicleId, request.getPayment(), diff);
        }
        if (request.getDelivery() != null) {
            updateDelivery(vehicleId, request.getDelivery(), diff);
        }
        if (request.getRegistration() != null) {
            updateRegistration(vehicleId, request.getRegistration(), diff);
        }
        auditService.recordSuccess(diff);
    }

    private boolean hasCorrectionSection(VehicleCorrectionUpdateRequest request) {
        return request.getProduction() != null
                || request.getInbound() != null
                || request.getAllocation() != null
                || (request.getInvoices() != null && !request.getInvoices().isEmpty())
                || request.getPayment() != null
                || request.getDelivery() != null
                || request.getRegistration() != null;
    }

    private void validateInvoiceIds(VehicleCorrectionUpdateRequest request) {
        if (request.getInvoices() == null) {
            return;
        }
        Set<Long> invoiceIds = new HashSet<>();
        for (InvoiceCorrection invoice : request.getInvoices()) {
            if (invoice == null || invoice.getId() == null) {
                throw badRequest("发票记录 ID 不能为空");
            }
            if (!invoiceIds.add(invoice.getId())) {
                throw badRequest("发票记录重复");
            }
        }
    }

    private void updateProduction(Long vehicleId, ProductionCorrection change, CorrectionDiff diff) {
        requireId(change.getId(), "生产记录 ID");
        VehProduction entity = vehProductionMapper.selectByIdForUpdate(change.getId());
        requireOwned(entity, entity == null ? null : entity.getVehicleId(), vehicleId);
        requireRequiredId(change.getModelId(), "车型");
        requireRequiredId(change.getExteriorColorId(), "外饰颜色");
        requireRequiredId(change.getInteriorColorId(), "内饰颜色");
        requireActive(vehicleModelMapper.selectCount(new LambdaQueryWrapper<VehicleModel>()
                .eq(VehicleModel::getId, change.getModelId())
                .eq(VehicleModel::getStatus, 1)
                .eq(VehicleModel::getDeleted, 0)), "车型");
        requireActive(exteriorColorMapper.selectCount(new LambdaQueryWrapper<ExteriorColor>()
                .eq(ExteriorColor::getId, change.getExteriorColorId())
                .eq(ExteriorColor::getStatus, 1)
                .eq(ExteriorColor::getDeleted, 0)), "外饰颜色");
        requireActive(interiorColorMapper.selectCount(new LambdaQueryWrapper<InteriorColor>()
                .eq(InteriorColor::getId, change.getInteriorColorId())
                .eq(InteriorColor::getStatus, 1)
                .eq(InteriorColor::getDeleted, 0)), "内饰颜色");

        diff.before("production", entity.getId(), productionValues(entity));
        entity.setModelId(change.getModelId());
        entity.setExteriorColorId(change.getExteriorColorId());
        entity.setInteriorColorId(change.getInteriorColorId());
        entity.setEngineNumber(change.getEngineNumber());
        entity.setYearMake(change.getYearMake());
        entity.setMaterial(change.getMaterial());
        entity.setShipment(change.getShipment());
        entity.setBatch(change.getBatch());
        entity.setOfflineEpmbDate(change.getOfflineEpmbDate());
        entity.setEpmbOkDate(change.getEpmbOkDate());
        entity.setRemark1(change.getRemark1());
        requireSingleStageUpdate(vehProductionMapper.update(null,
                new LambdaUpdateWrapper<VehProduction>()
                        .eq(VehProduction::getId, entity.getId())
                        .eq(VehProduction::getDeleted, 0)
                        .set(VehProduction::getModelId, change.getModelId())
                        .set(VehProduction::getExteriorColorId, change.getExteriorColorId())
                        .set(VehProduction::getInteriorColorId, change.getInteriorColorId())
                        .set(VehProduction::getEngineNumber, change.getEngineNumber())
                        .set(VehProduction::getYearMake, change.getYearMake())
                        .set(VehProduction::getMaterial, change.getMaterial())
                        .set(VehProduction::getShipment, change.getShipment())
                        .set(VehProduction::getBatch, change.getBatch())
                        .set(VehProduction::getOfflineEpmbDate, change.getOfflineEpmbDate())
                        .set(VehProduction::getEpmbOkDate, change.getEpmbOkDate())
                        .set(VehProduction::getRemark1, change.getRemark1())));
        diff.after("production", entity.getId(), productionValues(entity));
    }

    private void updateInbound(Long vehicleId, InboundCorrection change, CorrectionDiff diff) {
        requireId(change.getId(), "入库记录 ID");
        VehInbound entity = vehInboundMapper.selectByIdForUpdate(change.getId());
        requireOwned(entity, entity == null ? null : entity.getVehicleId(), vehicleId);
        if ("CONFIRMED".equals(entity.getStageStatus())
                && (change.getSaicBuyOffDate() == null || change.getDateToStorageYard() == null)) {
            throw badRequest("已确认入库记录的 SAIC Buy Off 日期和入库日期不能为空");
        }
        diff.before("inbound", entity.getId(), inboundValues(entity));
        entity.setSaicBuyOffDate(change.getSaicBuyOffDate());
        entity.setDateToStorageYard(change.getDateToStorageYard());
        entity.setRemark2(change.getRemark2());
        requireSingleStageUpdate(vehInboundMapper.update(null,
                new LambdaUpdateWrapper<VehInbound>()
                        .eq(VehInbound::getId, entity.getId())
                        .eq(VehInbound::getDeleted, 0)
                        .set(VehInbound::getSaicBuyOffDate, change.getSaicBuyOffDate())
                        .set(VehInbound::getDateToStorageYard, change.getDateToStorageYard())
                        .set(VehInbound::getRemark2, change.getRemark2())));
        diff.after("inbound", entity.getId(), inboundValues(entity));
    }

    private void updateAllocation(Long vehicleId, AllocationCorrection change, CorrectionDiff diff) {
        requireId(change.getId(), "分配记录 ID");
        VehAllocation entity = vehAllocationMapper.selectByIdForUpdate(change.getId());
        requireOwned(entity, entity == null ? null : entity.getVehicleId(), vehicleId);
        requireRequiredId(change.getDealerId(), "经销商");
        requireActive(dealerMapper.selectCount(new LambdaQueryWrapper<Dealer>()
                .eq(Dealer::getId, change.getDealerId())
                .eq(Dealer::getStatus, 1)
                .eq(Dealer::getDeleted, 0)), "经销商");
        requireDictionary("sales_status", change.getSalesStatus());
        diff.before("allocation", entity.getId(), allocationValues(entity));
        entity.setAllocatedDate(change.getAllocatedDate());
        entity.setDealerId(change.getDealerId());
        entity.setSalesStatus(change.getSalesStatus());
        entity.setRemark3(change.getRemark3());
        requireSingleStageUpdate(vehAllocationMapper.update(null,
                new LambdaUpdateWrapper<VehAllocation>()
                        .eq(VehAllocation::getId, entity.getId())
                        .eq(VehAllocation::getDeleted, 0)
                        .set(VehAllocation::getAllocatedDate, change.getAllocatedDate())
                        .set(VehAllocation::getDealerId, change.getDealerId())
                        .set(VehAllocation::getSalesStatus, change.getSalesStatus())
                        .set(VehAllocation::getRemark3, change.getRemark3())));
        diff.after("allocation", entity.getId(), allocationValues(entity));
    }

    private void updateInvoice(Long vehicleId, InvoiceCorrection change, CorrectionDiff diff) {
        VehInvoice entity = vehInvoiceMapper.selectByIdForUpdate(change.getId());
        requireOwned(entity, entity == null ? null : entity.getVehicleId(), vehicleId);
        requireDictionary("invoice_status", change.getInvoiceType());
        diff.before("invoice", entity.getId(), invoiceValues(entity));
        entity.setInvoiceType(change.getInvoiceType());
        entity.setInvoiceNo(change.getInvoiceNo());
        entity.setInvoiceDate(change.getInvoiceDate());
        entity.setRemark(change.getRemark());
        requireSingleStageUpdate(vehInvoiceMapper.update(null,
                new LambdaUpdateWrapper<VehInvoice>()
                        .eq(VehInvoice::getId, entity.getId())
                        .eq(VehInvoice::getDeleted, 0)
                        .set(VehInvoice::getInvoiceType, change.getInvoiceType())
                        .set(VehInvoice::getInvoiceNo, change.getInvoiceNo())
                        .set(VehInvoice::getInvoiceDate, change.getInvoiceDate())
                        .set(VehInvoice::getRemark, change.getRemark())));
        diff.after("invoice", entity.getId(), invoiceValues(entity));
    }

    private void updatePayment(Long vehicleId, PaymentCorrection change, CorrectionDiff diff) {
        requireId(change.getId(), "收款记录 ID");
        VehPayment entity = vehPaymentMapper.selectByIdForUpdate(change.getId());
        requireOwned(entity, entity == null ? null : entity.getVehicleId(), vehicleId);
        requireDictionary("payment_status", change.getPaymentStatus());
        diff.before("payment", entity.getId(), paymentValues(entity));
        entity.setPaymentDate(change.getPaymentDate());
        entity.setCreditFullPaymentDate(change.getCreditFullPaymentDate());
        entity.setPaymentStatus(change.getPaymentStatus());
        entity.setRemark5(change.getRemark5());
        requireSingleStageUpdate(vehPaymentMapper.update(null,
                new LambdaUpdateWrapper<VehPayment>()
                        .eq(VehPayment::getId, entity.getId())
                        .eq(VehPayment::getDeleted, 0)
                        .set(VehPayment::getPaymentDate, change.getPaymentDate())
                        .set(VehPayment::getCreditFullPaymentDate, change.getCreditFullPaymentDate())
                        .set(VehPayment::getPaymentStatus, change.getPaymentStatus())
                        .set(VehPayment::getRemark5, change.getRemark5())));
        diff.after("payment", entity.getId(), paymentValues(entity));
    }

    private void updateDelivery(Long vehicleId, DeliveryCorrection change, CorrectionDiff diff) {
        requireId(change.getId(), "配送记录 ID");
        VehDelivery entity = vehDeliveryMapper.selectByIdForUpdate(change.getId());
        requireOwned(entity, entity == null ? null : entity.getVehicleId(), vehicleId);
        if ("CONFIRMED".equals(entity.getStageStatus()) && change.getReceivedDate() == null) {
            throw badRequest("已确认配送记录的签收日期不能为空");
        }
        if (change.getEtdToDealer() != null && change.getEtaToDealer() != null
                && change.getEtaToDealer().isBefore(change.getEtdToDealer())) {
            throw badRequest("预计到达日期不能早于发车日期");
        }
        if (change.getTrollyType() != null
                && !"4 units".equals(change.getTrollyType())
                && !"6 units".equals(change.getTrollyType())) {
            throw badRequest("拖运车类型只能为4 units或6 units");
        }
        requireDictionary("delivery_status", change.getDeliveryStatus());
        diff.before("delivery", entity.getId(), deliveryValues(entity));
        entity.setEtdToDealer(change.getEtdToDealer());
        entity.setEtaToDealer(change.getEtaToDealer());
        entity.setTrollyType(change.getTrollyType());
        entity.setFullyLoad(change.getFullyLoad());
        entity.setReceivedDate(change.getReceivedDate());
        entity.setDeliveryStatus(change.getDeliveryStatus());
        entity.setRemark7(change.getRemark7());
        requireSingleStageUpdate(vehDeliveryMapper.update(null,
                new LambdaUpdateWrapper<VehDelivery>()
                        .eq(VehDelivery::getId, entity.getId())
                        .eq(VehDelivery::getDeleted, 0)
                        .set(VehDelivery::getEtdToDealer, change.getEtdToDealer())
                        .set(VehDelivery::getEtaToDealer, change.getEtaToDealer())
                        .set(VehDelivery::getTrollyType, change.getTrollyType())
                        .set(VehDelivery::getFullyLoad, change.getFullyLoad())
                        .set(VehDelivery::getReceivedDate, change.getReceivedDate())
                        .set(VehDelivery::getDeliveryStatus, change.getDeliveryStatus())
                        .set(VehDelivery::getRemark7, change.getRemark7())));
        diff.after("delivery", entity.getId(), deliveryValues(entity));
    }

    private void updateRegistration(Long vehicleId, RegistrationCorrection change, CorrectionDiff diff) {
        requireId(change.getId(), "上牌记录 ID");
        VehRegistration entity = vehRegistrationMapper.selectByIdForUpdate(change.getId());
        requireOwned(entity, entity == null ? null : entity.getVehicleId(), vehicleId);
        requireDictionary("drosstech_status", change.getDrosstechStatus());
        diff.before("registration", entity.getId(), registrationValues(entity));
        entity.setDrosstechStatus(change.getDrosstechStatus());
        entity.setUploadDate(change.getUploadDate());
        entity.setRegistrationDate(change.getRegistrationDate());
        entity.setCustomerRegion(change.getCustomerRegion());
        entity.setRemark8(change.getRemark8());
        requireSingleStageUpdate(vehRegistrationMapper.update(null,
                new LambdaUpdateWrapper<VehRegistration>()
                        .eq(VehRegistration::getId, entity.getId())
                        .eq(VehRegistration::getDeleted, 0)
                        .set(VehRegistration::getDrosstechStatus, change.getDrosstechStatus())
                        .set(VehRegistration::getUploadDate, change.getUploadDate())
                        .set(VehRegistration::getRegistrationDate, change.getRegistrationDate())
                        .set(VehRegistration::getCustomerRegion, change.getCustomerRegion())
                        .set(VehRegistration::getRemark8, change.getRemark8())));
        diff.after("registration", entity.getId(), registrationValues(entity));
    }

    private void requireSingleStageUpdate(int updatedRows) {
        if (updatedRows != 1) {
            throw new BusinessException(ErrorCode.STAGE_DATA_NOT_FOUND);
        }
    }

    private <T> T requireOwned(T entity, Long ownerVehicleId, Long requestedVehicleId) {
        if (entity == null) {
            throw new BusinessException(ErrorCode.STAGE_DATA_NOT_FOUND);
        }
        if (!requestedVehicleId.equals(ownerVehicleId)) {
            throw new BusinessException(ErrorCode.CORRECTION_RECORD_MISMATCH);
        }
        return entity;
    }

    private void requireDictionary(String dictCode, String value) {
        if (value == null) {
            return;
        }
        if (value.isBlank()) {
            throw badRequest("字典值不能为空: " + dictCode);
        }
        Map<String, String> labels = statusLabelService.dictLabels(dictCode);
        if (labels == null || !labels.containsKey(value)) {
            throw badRequest("无效字典值: " + dictCode + "=" + value);
        }
    }

    private void requireActive(Long count, String name) {
        if (count == null || count == 0) {
            throw badRequest(name + "不存在或已停用");
        }
    }

    private void requireRequiredId(Long id, String name) {
        if (id == null) {
            throw badRequest(name + " ID 不能为空");
        }
    }

    private void requireId(Long id, String name) {
        if (id == null) {
            throw badRequest(name + "不能为空");
        }
    }

    private BusinessException badRequest(String message) {
        return new BusinessException(ErrorCode.BAD_REQUEST.getCode(), message);
    }

    private Map<String, Object> productionValues(VehProduction entity) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("modelId", entity.getModelId());
        values.put("exteriorColorId", entity.getExteriorColorId());
        values.put("interiorColorId", entity.getInteriorColorId());
        values.put("engineNumber", entity.getEngineNumber());
        values.put("yearMake", entity.getYearMake());
        values.put("material", entity.getMaterial());
        values.put("shipment", entity.getShipment());
        values.put("batch", entity.getBatch());
        values.put("offlineEpmbDate", entity.getOfflineEpmbDate());
        values.put("epmbOkDate", entity.getEpmbOkDate());
        values.put("remark1", entity.getRemark1());
        return values;
    }

    private Map<String, Object> inboundValues(VehInbound entity) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("saicBuyOffDate", entity.getSaicBuyOffDate());
        values.put("dateToStorageYard", entity.getDateToStorageYard());
        values.put("remark2", entity.getRemark2());
        return values;
    }

    private Map<String, Object> allocationValues(VehAllocation entity) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("allocatedDate", entity.getAllocatedDate());
        values.put("dealerId", entity.getDealerId());
        values.put("salesStatus", entity.getSalesStatus());
        values.put("remark3", entity.getRemark3());
        return values;
    }

    private Map<String, Object> invoiceValues(VehInvoice entity) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("invoiceType", entity.getInvoiceType());
        values.put("invoiceNo", entity.getInvoiceNo());
        values.put("invoiceDate", entity.getInvoiceDate());
        values.put("remark", entity.getRemark());
        return values;
    }

    private Map<String, Object> paymentValues(VehPayment entity) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("paymentDate", entity.getPaymentDate());
        values.put("creditFullPaymentDate", entity.getCreditFullPaymentDate());
        values.put("paymentStatus", entity.getPaymentStatus());
        values.put("remark5", entity.getRemark5());
        return values;
    }

    private Map<String, Object> deliveryValues(VehDelivery entity) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("etdToDealer", entity.getEtdToDealer());
        values.put("etaToDealer", entity.getEtaToDealer());
        values.put("trollyType", entity.getTrollyType());
        values.put("fullyLoad", entity.getFullyLoad());
        values.put("receivedDate", entity.getReceivedDate());
        values.put("deliveryStatus", entity.getDeliveryStatus());
        values.put("remark7", entity.getRemark7());
        return values;
    }

    private Map<String, Object> registrationValues(VehRegistration entity) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("drosstechStatus", entity.getDrosstechStatus());
        values.put("uploadDate", entity.getUploadDate());
        values.put("registrationDate", entity.getRegistrationDate());
        values.put("customerRegion", entity.getCustomerRegion());
        values.put("remark8", entity.getRemark8());
        return values;
    }

    private void prepareRows(List<VehicleCorrectionListResponse> rows, long firstNo) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        Map<String, String> invoiceLabels = statusLabelService.dictLabels("invoice_status");
        Map<String, String> paymentLabels = statusLabelService.dictLabels("payment_status");
        Map<String, String> deliveryLabels = statusLabelService.dictLabels("delivery_status");
        Map<String, String> drosstechLabels = statusLabelService.dictLabels("drosstech_status");
        for (int index = 0; index < rows.size(); index++) {
            VehicleCorrectionListResponse row = rows.get(index);
            row.setNo(firstNo + index);
            row.setStatus1(label(invoiceLabels, row.getStatus1()));
            row.setStatus2(label(invoiceLabels, row.getStatus2()));
            row.setPaymentStatus(label(paymentLabels, row.getPaymentStatus()));
            row.setDeliveryStatus(label(deliveryLabels, row.getDeliveryStatus()));
            row.setDrosstechStatus(label(drosstechLabels, row.getDrosstechStatus()));
            row.setFullyLoad(row.getFullyLoadValue() == null
                    ? null : Boolean.TRUE.equals(row.getFullyLoadValue()) ? "Full" : "Not Full");
        }
    }

    private String label(Map<String, String> labels, String value) {
        return value == null || labels == null ? value : labels.getOrDefault(value, value);
    }
}
