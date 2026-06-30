package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.InvoiceConvertRequest;
import com.company.admin.dto.request.InvoiceCreateRequest;
import com.company.admin.dto.request.InvoiceQueryRequest;
import com.company.admin.dto.response.InvoiceListResponse;
import com.company.admin.dto.response.InvoiceResponse;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.StageStatus;
import com.company.admin.entity.VehInvoice;
import com.company.admin.mapper.VehInvoiceMapper;
import com.company.admin.service.LifecycleService;
import com.company.admin.service.VehInvoiceService;
import com.company.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehInvoiceServiceImpl implements VehInvoiceService {

    private static final String FORMAL_INVOICE = "INVOICED";
    private static final String PROFORMA_INVOICE = "PROFORMA_INVOICED";

    private final VehInvoiceMapper vehInvoiceMapper;
    private final LifecycleService lifecycleService;

    @Override
    public PageResult<InvoiceListResponse> pageInvoices(InvoiceQueryRequest request) {
        Page<InvoiceListResponse> page = vehInvoiceMapper.selectInvoicePage(
                new Page<>(request.getPageNum(), request.getPageSize()), request);
        return new PageResult<>(page.getRecords(), page.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    @Transactional
    public Long createInvoice(Long vehicleId, InvoiceCreateRequest request) {
        lifecycleService.assertStage(vehicleId, LifecycleStage.PENDING_INVOICE);
        if (vehInvoiceMapper.selectCount(new LambdaQueryWrapper<VehInvoice>()
                .eq(VehInvoice::getVehicleId, vehicleId)
                .eq(VehInvoice::getDeleted, 0)) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "车辆已存在发票记录");
        }
        VehInvoice invoice = new VehInvoice();
        copyCreateFields(request, invoice);
        invoice.setVehicleId(vehicleId);
        invoice.setInvoiceSeq(1);
        invoice.setStageStatus(StageStatus.DRAFT.name());
        vehInvoiceMapper.insert(invoice);
        return invoice.getId();
    }

    @Override
    @Transactional
    public void updateInvoice(Long id, InvoiceCreateRequest request) {
        VehInvoice invoice = getInvoiceEntity(id);
        lifecycleService.assertNotConfirmed(invoice.getStageStatus());
        copyCreateFields(request, invoice);
        vehInvoiceMapper.updateById(invoice);
    }

    @Override
    @Transactional
    public void confirmInvoice(Long id) {
        VehInvoice invoice = getInvoiceEntity(id);
        lifecycleService.assertNotConfirmed(invoice.getStageStatus());

        if (Integer.valueOf(1).equals(invoice.getInvoiceSeq())) {
            lifecycleService.assertStage(invoice.getVehicleId(), LifecycleStage.PENDING_INVOICE);
            markConfirmed(invoice);
            vehInvoiceMapper.updateById(invoice);
            lifecycleService.advanceStage(
                    invoice.getVehicleId(),
                    LifecycleStage.PENDING_INVOICE,
                    LifecycleStage.PENDING_PAYMENT);
            return;
        }

        if (Integer.valueOf(2).equals(invoice.getInvoiceSeq())) {
            lifecycleService.assertStage(invoice.getVehicleId(), LifecycleStage.PENDING_PAYMENT);
            if (!FORMAL_INVOICE.equals(invoice.getInvoiceType())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "转正发票必须为正式发票");
            }
            markConfirmed(invoice);
            vehInvoiceMapper.updateById(invoice);
            return;
        }

        throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "发票序号不合法");
    }

    @Override
    @Transactional
    public Long convertProforma(Long vehicleId, InvoiceConvertRequest request) {
        lifecycleService.assertStage(vehicleId, LifecycleStage.PENDING_PAYMENT);
        VehInvoice latest = getLatestInvoice(vehicleId);
        if (!PROFORMA_INVOICE.equals(latest.getInvoiceType())) {
            throw new BusinessException(ErrorCode.INVOICE_NOT_FORMAL);
        }
        VehInvoice invoice = new VehInvoice();
        invoice.setVehicleId(vehicleId);
        invoice.setInvoiceSeq(2);
        invoice.setInvoiceType(FORMAL_INVOICE);
        invoice.setInvoiceNo(request.getInvoiceNo());
        invoice.setInvoiceDate(request.getInvoiceDate());
        invoice.setRemark(request.getRemark());
        invoice.setStageStatus(StageStatus.DRAFT.name());
        vehInvoiceMapper.insert(invoice);
        return invoice.getId();
    }

    @Override
    public List<InvoiceResponse> getInvoices(Long vehicleId) {
        return listInvoices(vehicleId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    private VehInvoice getInvoiceEntity(Long id) {
        VehInvoice invoice = vehInvoiceMapper.selectById(id);
        if (invoice == null) {
            throw new BusinessException(ErrorCode.STAGE_DATA_NOT_FOUND);
        }
        return invoice;
    }

    private VehInvoice getLatestInvoice(Long vehicleId) {
        List<VehInvoice> invoices = listInvoices(vehicleId);
        if (invoices.isEmpty()) {
            throw new BusinessException(ErrorCode.STAGE_DATA_NOT_FOUND);
        }
        return invoices.get(0);
    }

    private List<VehInvoice> listInvoices(Long vehicleId) {
        return vehInvoiceMapper.selectList(new LambdaQueryWrapper<VehInvoice>()
                .eq(VehInvoice::getVehicleId, vehicleId)
                .eq(VehInvoice::getDeleted, 0)
                .orderByDesc(VehInvoice::getInvoiceSeq));
    }

    private void copyCreateFields(InvoiceCreateRequest request, VehInvoice invoice) {
        invoice.setInvoiceType(request.getInvoiceType());
        invoice.setInvoiceNo(request.getInvoiceNo());
        invoice.setInvoiceDate(request.getInvoiceDate());
        invoice.setRemark(request.getRemark());
    }

    private void markConfirmed(VehInvoice invoice) {
        invoice.setStageStatus(StageStatus.CONFIRMED.name());
        invoice.setConfirmedBy(SecurityUtils.getCurrentUsername());
        invoice.setConfirmedAt(LocalDateTime.now());
    }

    private InvoiceResponse toResponse(VehInvoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        BeanUtils.copyProperties(invoice, response);
        return response;
    }
}
