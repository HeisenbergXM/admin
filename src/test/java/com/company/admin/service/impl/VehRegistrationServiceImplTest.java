package com.company.admin.service.impl;

import com.company.admin.dto.request.RegistrationSaveRequest;
import com.company.admin.entity.VehRegistration;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.StageStatus;
import com.company.admin.mapper.VehRegistrationMapper;
import com.company.admin.service.LifecycleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehRegistrationServiceImplTest {

    @Mock
    private VehRegistrationMapper vehRegistrationMapper;

    @Mock
    private LifecycleService lifecycleService;

    @InjectMocks
    private VehRegistrationServiceImpl service;

    @Test
    void createRegistrationChecksPendingRegistrationAndStoresDraft() {
        doAnswer(invocation -> {
            VehRegistration registration = invocation.getArgument(0);
            registration.setId(16L);
            return 1;
        }).when(vehRegistrationMapper).insert(any(VehRegistration.class));

        Long id = service.createRegistration(99L, saveRequest());

        assertEquals(16L, id);
        verify(lifecycleService).assertStage(99L, LifecycleStage.PENDING_REGISTRATION);
        ArgumentCaptor<VehRegistration> captor = ArgumentCaptor.forClass(VehRegistration.class);
        verify(vehRegistrationMapper).insert(captor.capture());
        assertEquals(StageStatus.DRAFT.name(), captor.getValue().getStageStatus());
        assertEquals("UPLOADED", captor.getValue().getDrosstechStatus());
    }

    @Test
    void confirmRegistrationLocksAndCompletesLifecycle() {
        VehRegistration registration = draftRegistration();
        when(vehRegistrationMapper.selectOne(any())).thenReturn(registration);
        doAnswer(invocation -> {
            Runnable lockAction = invocation.getArgument(4);
            lockAction.run();
            return null;
        }).when(lifecycleService).confirmAndAdvance(
                eq(99L),
                eq(StageStatus.DRAFT.name()),
                eq(LifecycleStage.PENDING_REGISTRATION),
                eq(LifecycleStage.COMPLETED),
                any(Runnable.class));

        service.confirmRegistration(99L);

        ArgumentCaptor<VehRegistration> captor = ArgumentCaptor.forClass(VehRegistration.class);
        verify(vehRegistrationMapper).updateById(captor.capture());
        assertEquals(StageStatus.CONFIRMED.name(), captor.getValue().getStageStatus());
        assertNotNull(captor.getValue().getConfirmedAt());
    }

    private RegistrationSaveRequest saveRequest() {
        RegistrationSaveRequest request = new RegistrationSaveRequest();
        request.setDrosstechStatus("UPLOADED");
        request.setUploadDate(LocalDate.of(2026, 7, 7));
        request.setRegistrationDate(LocalDate.of(2026, 7, 8));
        request.setCustomerRegion("North");
        request.setRemark8("remark");
        return request;
    }

    private VehRegistration draftRegistration() {
        VehRegistration registration = new VehRegistration();
        registration.setId(16L);
        registration.setVehicleId(99L);
        registration.setStageStatus(StageStatus.DRAFT.name());
        return registration;
    }
}
