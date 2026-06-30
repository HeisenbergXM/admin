package com.company.admin.service;

import com.company.admin.common.PageResult;
import com.company.admin.dto.request.RegistrationQueryRequest;
import com.company.admin.dto.request.RegistrationSaveRequest;
import com.company.admin.dto.response.RegistrationResponse;

public interface VehRegistrationService {

    PageResult<RegistrationResponse> pageRegistrations(RegistrationQueryRequest request);

    Long createRegistration(Long vehicleId, RegistrationSaveRequest request);

    void updateRegistration(Long vehicleId, RegistrationSaveRequest request);

    void confirmRegistration(Long vehicleId);

    RegistrationResponse getRegistration(Long vehicleId);
}
