package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.dto.request.DispatchListQueryRequest;
import com.company.admin.dto.request.DispatchListSaveRequest;
import com.company.admin.dto.request.WaybillDealerConfirmRequest;
import com.company.admin.dto.request.WaybillDealerSaveRequest;
import com.company.admin.dto.request.WaybillSaveRequest;
import com.company.admin.dto.response.DispatchListResponse;

public interface DispatchListService {

    PageResult<DispatchListResponse> pageDispatchLists(DispatchListQueryRequest request);

    Long createDispatchList(DispatchListSaveRequest request);

    DispatchListResponse getDetail(Long id);

    Long addWaybill(Long dispatchId, WaybillSaveRequest request);

    void updateWaybill(Long id, WaybillSaveRequest request);

    void deleteWaybill(Long id);

    Long addDealer(Long waybillId, WaybillDealerSaveRequest request);

    void updateDealer(Long id, WaybillDealerSaveRequest request);

    void deleteDealer(Long id);

    Long addVin(Long dealerRowId, Long vehicleId);

    void removeVin(Long dealerRowId, Long vinId);

    void confirmDealerRow(Long dealerRowId);

    void confirmDealerRow(Long dealerRowId, WaybillDealerConfirmRequest request);
}
