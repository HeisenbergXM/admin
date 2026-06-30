package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.dto.request.AllocationQueryRequest;
import com.company.admin.dto.request.AllocationSaveRequest;
import com.company.admin.dto.response.AllocationResponse;

public interface VehAllocationService {

    PageResult<AllocationResponse> pageAllocations(AllocationQueryRequest request);

    Long createAllocation(Long vehicleId, AllocationSaveRequest request);

    void updateAllocation(Long vehicleId, AllocationSaveRequest request);

    void confirmAllocation(Long vehicleId);

    AllocationResponse getAllocation(Long vehicleId);
}
