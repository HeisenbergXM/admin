package com.company.admin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.InboundQueryRequest;
import com.company.admin.dto.request.InboundSaveRequest;
import com.company.admin.dto.response.InboundResponse;
import com.company.admin.entity.VehInbound;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.StageStatus;
import com.company.admin.mapper.VehInboundMapper;
import com.company.admin.service.BusinessStatusLabelService;
import com.company.admin.service.LifecycleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehInboundServiceImplTest {

    @Mock
    private VehInboundMapper vehInboundMapper;

    @Mock
    private LifecycleService lifecycleService;

    @Mock
    private BusinessStatusLabelService statusLabelService;

    @InjectMocks
    private VehInboundServiceImpl service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void pageInboundsAppliesLifecycleAndStageStatusLabels() {
        InboundQueryRequest request = new InboundQueryRequest();
        request.setPageNum(2);
        request.setPageSize(5);
        InboundResponse inbound = new InboundResponse();
        inbound.setLifecycleStage(LifecycleStage.PENDING_INBOUND.name());
        inbound.setStageStatus(StageStatus.DRAFT.name());
        Page<InboundResponse> mapperPage = new Page<>(2, 5);
        mapperPage.setRecords(Collections.singletonList(inbound));
        mapperPage.setTotal(7);
        when(vehInboundMapper.selectInboundPage(any(), same(request))).thenReturn(mapperPage);
        when(statusLabelService.lifecycleStageLabel(LifecycleStage.PENDING_INBOUND.name())).thenReturn("待入库");
        when(statusLabelService.stageStatusLabel(StageStatus.DRAFT.name())).thenReturn("草稿");

        PageResult<InboundResponse> result = service.pageInbounds(request);

        assertEquals(7, result.getTotal());
        assertEquals(2, result.getPageNum());
        assertEquals(5, result.getPageSize());
        assertEquals("待入库", result.getList().get(0).getLifecycleStageLabel());
        assertEquals("草稿", result.getList().get(0).getStageStatusLabel());
    }

    @Test
    void createInboundChecksStageAndStoresDraft() {
        doAnswer(invocation -> {
            VehInbound value = invocation.getArgument(0);
            value.setId(31L);
            return 1;
        }).when(vehInboundMapper).insert(any(VehInbound.class));

        Long id = service.createInbound(30L, completeRequest());

        assertEquals(31L, id);
        verify(lifecycleService).assertStage(30L, LifecycleStage.PENDING_INBOUND);
        ArgumentCaptor<VehInbound> captor = ArgumentCaptor.forClass(VehInbound.class);
        verify(vehInboundMapper).insert(captor.capture());
        assertEquals(StageStatus.DRAFT.name(), captor.getValue().getStageStatus());
        assertEquals(LocalDate.of(2026, 7, 14), captor.getValue().getSaicBuyOffDate());
        assertEquals(LocalDate.of(2026, 7, 15), captor.getValue().getDateToStorageYard());
    }

    @Test
    void createInboundAllowsIncompleteDraft() {
        doAnswer(invocation -> {
            VehInbound value = invocation.getArgument(0);
            value.setId(31L);
            return 1;
        }).when(vehInboundMapper).insert(any(VehInbound.class));

        Long id = service.createInbound(30L, new InboundSaveRequest());

        assertEquals(31L, id);
        ArgumentCaptor<VehInbound> captor = ArgumentCaptor.forClass(VehInbound.class);
        verify(vehInboundMapper).insert(captor.capture());
        assertEquals(StageStatus.DRAFT.name(), captor.getValue().getStageStatus());
        assertNull(captor.getValue().getSaicBuyOffDate());
        assertNull(captor.getValue().getDateToStorageYard());
    }

    @Test
    void createInboundRejectsDuplicateRecord() {
        when(vehInboundMapper.selectOne(any())).thenReturn(draftInbound());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.createInbound(30L, completeRequest()));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(vehInboundMapper, never()).insert(any(VehInbound.class));
    }

    @Test
    void updateInboundChecksStageAndDraftStatus() {
        VehInbound inbound = draftInbound();
        when(vehInboundMapper.selectOne(any())).thenReturn(inbound);
        InboundSaveRequest request = completeRequest();
        request.setRemark2("updated");

        service.updateInbound(30L, request);

        verify(lifecycleService).assertNotConfirmed(StageStatus.DRAFT.name());
        verify(lifecycleService).assertStage(30L, LifecycleStage.PENDING_INBOUND);
        verify(vehInboundMapper).updateById(inbound);
        assertEquals("updated", inbound.getRemark2());
    }

    @Test
    void confirmInboundRejectsMissingSaicBuyOffDate() {
        VehInbound inbound = draftInbound();
        inbound.setDateToStorageYard(LocalDate.of(2026, 7, 15));
        when(vehInboundMapper.selectOne(any())).thenReturn(inbound);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.confirmInbound(30L));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(lifecycleService, never()).confirmAndAdvance(any(), any(), any(), any(), any());
    }

    @Test
    void confirmInboundRejectsMissingStorageYardDate() {
        VehInbound inbound = draftInbound();
        inbound.setSaicBuyOffDate(LocalDate.of(2026, 7, 14));
        when(vehInboundMapper.selectOne(any())).thenReturn(inbound);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.confirmInbound(30L));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(lifecycleService, never()).confirmAndAdvance(any(), any(), any(), any(), any());
    }

    @Test
    void confirmInboundLocksAndAdvancesToAllocation() {
        VehInbound inbound = draftInbound();
        inbound.setSaicBuyOffDate(LocalDate.of(2026, 7, 14));
        inbound.setDateToStorageYard(LocalDate.of(2026, 7, 15));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("inbound-operator", null, Collections.emptyList()));
        when(vehInboundMapper.selectOne(any())).thenReturn(inbound);
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(4).run();
            return null;
        }).when(lifecycleService).confirmAndAdvance(
                eq(30L), eq(StageStatus.DRAFT.name()), eq(LifecycleStage.PENDING_INBOUND),
                eq(LifecycleStage.PENDING_ALLOCATION), any(Runnable.class));

        service.confirmInbound(30L);

        assertEquals(StageStatus.CONFIRMED.name(), inbound.getStageStatus());
        assertEquals("inbound-operator", inbound.getConfirmedBy());
        assertNotNull(inbound.getConfirmedAt());
        verify(vehInboundMapper).updateById(inbound);
    }

    @Test
    void getInboundReturnsPendingVehicleWithoutStageRecordAndAppliesLabels() {
        InboundResponse inbound = new InboundResponse();
        inbound.setVehicleId(30L);
        inbound.setLifecycleStage(LifecycleStage.PENDING_INBOUND.name());
        inbound.setStageStatus(LifecycleStage.PENDING_INBOUND.name());
        when(vehInboundMapper.selectInboundByVehicleId(30L)).thenReturn(inbound);
        when(statusLabelService.lifecycleStageLabel(LifecycleStage.PENDING_INBOUND.name())).thenReturn("待入库");
        when(statusLabelService.stageStatusLabel(LifecycleStage.PENDING_INBOUND.name())).thenReturn("待入库");

        InboundResponse result = service.getInbound(30L);

        assertEquals("待入库", result.getLifecycleStageLabel());
        assertEquals("待入库", result.getStageStatusLabel());
    }

    @Test
    void getInboundRejectsVehicleOutsidePendingInboundWithoutStageRecord() {
        InboundResponse inbound = new InboundResponse();
        inbound.setVehicleId(30L);
        inbound.setLifecycleStage(LifecycleStage.PENDING_ALLOCATION.name());
        when(vehInboundMapper.selectInboundByVehicleId(30L)).thenReturn(inbound);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.getInbound(30L));

        assertEquals(ErrorCode.STAGE_DATA_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void getInboundRejectsMissingVehicle() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.getInbound(30L));

        assertEquals(ErrorCode.STAGE_DATA_NOT_FOUND.getCode(), ex.getCode());
    }

    private InboundSaveRequest completeRequest() {
        InboundSaveRequest request = new InboundSaveRequest();
        request.setSaicBuyOffDate(LocalDate.of(2026, 7, 14));
        request.setDateToStorageYard(LocalDate.of(2026, 7, 15));
        return request;
    }

    private VehInbound draftInbound() {
        VehInbound inbound = new VehInbound();
        inbound.setId(31L);
        inbound.setVehicleId(30L);
        inbound.setStageStatus(StageStatus.DRAFT.name());
        return inbound;
    }
}
