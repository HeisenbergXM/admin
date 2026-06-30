package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.DispatchListQueryRequest;
import com.company.admin.dto.request.DispatchListSaveRequest;
import com.company.admin.dto.request.WaybillDealerConfirmRequest;
import com.company.admin.dto.request.WaybillDealerSaveRequest;
import com.company.admin.dto.request.WaybillSaveRequest;
import com.company.admin.dto.response.DispatchListResponse;
import com.company.admin.dto.response.VehicleBasicInfo;
import com.company.admin.dto.response.WaybillDealerResponse;
import com.company.admin.dto.response.WaybillResponse;
import com.company.admin.entity.DispatchList;
import com.company.admin.entity.Waybill;
import com.company.admin.entity.WaybillDealer;
import com.company.admin.entity.WaybillDealerVin;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.OrderStatus;
import com.company.admin.mapper.DispatchListMapper;
import com.company.admin.mapper.WaybillDealerMapper;
import com.company.admin.mapper.WaybillDealerVinMapper;
import com.company.admin.mapper.WaybillMapper;
import com.company.admin.service.DispatchListService;
import com.company.admin.service.LifecycleService;
import com.company.admin.service.VehicleBasicService;
import com.company.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DispatchListServiceImpl implements DispatchListService {

    private final DispatchListMapper dispatchListMapper;
    private final WaybillMapper waybillMapper;
    private final WaybillDealerMapper waybillDealerMapper;
    private final WaybillDealerVinMapper waybillDealerVinMapper;
    private final LifecycleService lifecycleService;
    private final VehicleBasicService vehicleBasicService;

    @Override
    public PageResult<DispatchListResponse> pageDispatchLists(DispatchListQueryRequest request) {
        Page<DispatchListResponse> page = dispatchListMapper.selectDispatchPage(
                new Page<>(request.getPageNum(), request.getPageSize()), request);
        return new PageResult<>(page.getRecords(), page.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    @Transactional
    public Long createDispatchList(DispatchListSaveRequest request) {
        DispatchList dispatchList = new DispatchList();
        dispatchList.setDispatchNo(request.getDispatchNo());
        dispatchList.setListStatus(OrderStatus.DRAFT.name());
        dispatchListMapper.insert(dispatchList);
        return dispatchList.getId();
    }

    @Override
    public DispatchListResponse getDetail(Long id) {
        DispatchList dispatchList = getDispatchList(id);
        List<Waybill> waybills = waybillMapper.selectByDispatchListId(id);
        List<WaybillDealer> dealers = waybillDealerMapper.selectByDispatchListId(id);
        Map<Long, List<WaybillDealer>> dealersByWaybill = dealers.stream()
                .collect(Collectors.groupingBy(WaybillDealer::getWaybillId));

        DispatchListResponse response = new DispatchListResponse();
        BeanUtils.copyProperties(dispatchList, response);
        response.setWaybills(waybills.stream().map(waybill -> {
            WaybillResponse waybillResponse = new WaybillResponse();
            BeanUtils.copyProperties(waybill, waybillResponse);
            waybillResponse.setDealers(dealersByWaybill.getOrDefault(waybill.getId(), List.of())
                    .stream().map(this::toDealerResponse).collect(Collectors.toList()));
            return waybillResponse;
        }).collect(Collectors.toList()));
        return response;
    }

    @Override
    @Transactional
    public Long addWaybill(Long dispatchId, WaybillSaveRequest request) {
        getDispatchList(dispatchId);
        Waybill waybill = new Waybill();
        copyWaybill(request, waybill);
        waybill.setDispatchListId(dispatchId);
        waybillMapper.insert(waybill);
        return waybill.getId();
    }

    @Override
    @Transactional
    public void updateWaybill(Long id, WaybillSaveRequest request) {
        Waybill waybill = getWaybill(id);
        copyWaybill(request, waybill);
        waybillMapper.updateById(waybill);
    }

    @Override
    @Transactional
    public void deleteWaybill(Long id) {
        getWaybill(id);
        waybillMapper.deleteById(id);
    }

    @Override
    @Transactional
    public Long addDealer(Long waybillId, WaybillDealerSaveRequest request) {
        getWaybill(waybillId);
        WaybillDealer dealer = new WaybillDealer();
        copyDealer(request, dealer);
        dealer.setWaybillId(waybillId);
        dealer.setRowStatus(OrderStatus.DRAFT.name());
        waybillDealerMapper.insert(dealer);
        return dealer.getId();
    }

    @Override
    @Transactional
    public void updateDealer(Long id, WaybillDealerSaveRequest request) {
        WaybillDealer dealer = getDealer(id);
        assertDealerDraft(dealer);
        copyDealer(request, dealer);
        waybillDealerMapper.updateById(dealer);
    }

    @Override
    @Transactional
    public void deleteDealer(Long id) {
        WaybillDealer dealer = getDealer(id);
        assertDealerDraft(dealer);
        waybillDealerMapper.deleteById(id);
    }

    @Override
    @Transactional
    public Long addVin(Long dealerRowId, Long vehicleId) {
        WaybillDealer dealer = getDealer(dealerRowId);
        assertDealerDraft(dealer);
        lifecycleService.assertStage(vehicleId, LifecycleStage.PENDING_DELIVERY);
        VehicleBasicInfo basicInfo = vehicleBasicService.getBasicInfo(vehicleId);
        if (basicInfo.getDealerId() == null || !basicInfo.getDealerId().equals(dealer.getDealerId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "车辆不属于该经销商");
        }
        if (waybillDealerVinMapper.countOpenVin(vehicleId, dealerRowId) > 0) {
            throw new BusinessException(ErrorCode.VEHICLE_OCCUPIED_BY_OTHER_ORDER);
        }
        WaybillDealerVin vin = new WaybillDealerVin();
        vin.setWaybillDealerId(dealerRowId);
        vin.setVehicleId(vehicleId);
        waybillDealerVinMapper.insert(vin);
        return vin.getId();
    }

    @Override
    @Transactional
    public void removeVin(Long dealerRowId, Long vinId) {
        WaybillDealer dealer = getDealer(dealerRowId);
        assertDealerDraft(dealer);
        WaybillDealerVin vin = waybillDealerVinMapper.selectOne(new LambdaQueryWrapper<WaybillDealerVin>()
                .eq(WaybillDealerVin::getId, vinId)
                .eq(WaybillDealerVin::getWaybillDealerId, dealerRowId));
        if (vin == null) {
            throw new BusinessException(ErrorCode.STAGE_DATA_NOT_FOUND);
        }
        waybillDealerVinMapper.deleteById(vinId);
    }

    @Override
    public void confirmDealerRow(Long dealerRowId) {
        confirmDealerRow(dealerRowId, null);
    }

    @Override
    @Transactional
    public void confirmDealerRow(Long dealerRowId, WaybillDealerConfirmRequest request) {
        WaybillDealer dealer = getDealer(dealerRowId);
        assertDealerDraft(dealer);
        List<WaybillDealerVin> vins = waybillDealerVinMapper.selectByDealerRowId(dealerRowId);
        if (vins.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        WaybillDealerVin first = vins.get(0);
        lifecycleService.confirmAndAdvance(
                first.getVehicleId(),
                dealer.getRowStatus(),
                LifecycleStage.PENDING_DELIVERY,
                LifecycleStage.PENDING_REGISTRATION,
                () -> lockDealerRow(dealer, request));
        for (int i = 1; i < vins.size(); i++) {
            Long vehicleId = vins.get(i).getVehicleId();
            lifecycleService.assertStage(vehicleId, LifecycleStage.PENDING_DELIVERY);
            lifecycleService.advanceStage(vehicleId, LifecycleStage.PENDING_DELIVERY, LifecycleStage.PENDING_REGISTRATION);
        }
    }

    private DispatchList getDispatchList(Long id) {
        DispatchList dispatchList = dispatchListMapper.selectById(id);
        if (dispatchList == null) {
            throw new BusinessException(ErrorCode.DISPATCH_LIST_NOT_FOUND);
        }
        return dispatchList;
    }

    private Waybill getWaybill(Long id) {
        Waybill waybill = waybillMapper.selectById(id);
        if (waybill == null) {
            throw new BusinessException(ErrorCode.WAYBILL_NOT_FOUND);
        }
        return waybill;
    }

    private WaybillDealer getDealer(Long id) {
        WaybillDealer dealer = waybillDealerMapper.selectById(id);
        if (dealer == null) {
            throw new BusinessException(ErrorCode.WAYBILL_DEALER_NOT_FOUND);
        }
        return dealer;
    }

    private void copyWaybill(WaybillSaveRequest request, Waybill waybill) {
        waybill.setWaybillNo(request.getWaybillNo());
        waybill.setTrollyType(request.getTrollyType());
        waybill.setFullyLoad(request.getFullyLoad());
    }

    private void copyDealer(WaybillDealerSaveRequest request, WaybillDealer dealer) {
        dealer.setDealerId(request.getDealerId());
        dealer.setEtdToDealer(request.getEtdToDealer());
        dealer.setEtaToDealer(request.getEtaToDealer());
        dealer.setReceivedDate(request.getReceivedDate());
        dealer.setDeliveryStatus(request.getDeliveryStatus());
        dealer.setRemark7(request.getRemark7());
    }

    private void assertDealerDraft(WaybillDealer dealer) {
        if (OrderStatus.CONFIRMED.name().equals(dealer.getRowStatus())) {
            throw new BusinessException(ErrorCode.STAGE_ALREADY_CONFIRMED);
        }
    }

    private void lockDealerRow(WaybillDealer dealer, WaybillDealerConfirmRequest request) {
        if (request != null) {
            if (request.getReceivedDate() != null) {
                dealer.setReceivedDate(request.getReceivedDate());
            }
            if (request.getDeliveryStatus() != null) {
                dealer.setDeliveryStatus(request.getDeliveryStatus());
            }
        }
        dealer.setRowStatus(OrderStatus.CONFIRMED.name());
        dealer.setConfirmedBy(SecurityUtils.getCurrentUsername());
        dealer.setConfirmedAt(LocalDateTime.now());
        waybillDealerMapper.updateById(dealer);
    }

    private WaybillDealerResponse toDealerResponse(WaybillDealer dealer) {
        WaybillDealerResponse response = new WaybillDealerResponse();
        BeanUtils.copyProperties(dealer, response);
        List<WaybillDealerVin> vins = waybillDealerVinMapper.selectByDealerRowId(dealer.getId());
        List<Long> vehicleIds = vins.stream().map(WaybillDealerVin::getVehicleId).collect(Collectors.toList());
        Map<Long, VehicleBasicInfo> vehicleMap = vehicleBasicService.getBasicInfoByIds(vehicleIds).stream()
                .collect(Collectors.toMap(VehicleBasicInfo::getId, Function.identity(), (a, b) -> a));
        response.setVins(vehicleIds.stream().map(vehicleMap::get).collect(Collectors.toList()));
        return response;
    }
}
