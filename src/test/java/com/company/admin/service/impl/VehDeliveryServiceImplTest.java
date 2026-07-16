package com.company.admin.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.DeliveryQueryRequest;
import com.company.admin.dto.request.DeliverySaveRequest;
import com.company.admin.dto.response.DeliveryResponse;
import com.company.admin.entity.VehAllocation;
import com.company.admin.entity.VehDelivery;
import com.company.admin.enums.LifecycleStage;
import com.company.admin.enums.StageStatus;
import com.company.admin.mapper.VehAllocationMapper;
import com.company.admin.mapper.VehDeliveryMapper;
import com.company.admin.service.BusinessStatusLabelService;
import com.company.admin.service.LifecycleService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehDeliveryServiceImplTest {

    @Mock
    private VehDeliveryMapper vehDeliveryMapper;

    @Mock
    private VehAllocationMapper vehAllocationMapper;

    @Mock
    private LifecycleService lifecycleService;

    @Mock
    private BusinessStatusLabelService statusLabelService;

    @InjectMocks
    private VehDeliveryServiceImpl service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void pageDeliveriesAppliesLifecycleStageAndDeliveryStatusLabels() {
        DeliveryQueryRequest request = new DeliveryQueryRequest();
        request.setPageNum(2);
        request.setPageSize(5);
        DeliveryResponse delivery = new DeliveryResponse();
        delivery.setLifecycleStage(LifecycleStage.PENDING_DELIVERY.name());
        delivery.setStageStatus(StageStatus.DRAFT.name());
        delivery.setDeliveryStatus("IN_TRANSIT");
        Page<DeliveryResponse> mapperPage = new Page<>(2, 5);
        mapperPage.setRecords(Collections.singletonList(delivery));
        mapperPage.setTotal(7);
        when(vehDeliveryMapper.selectDeliveryPage(any(), same(request))).thenReturn(mapperPage);
        when(statusLabelService.lifecycleStageLabel(LifecycleStage.PENDING_DELIVERY.name())).thenReturn("待配送");
        when(statusLabelService.stageStatusLabel(StageStatus.DRAFT.name())).thenReturn("草稿");
        when(statusLabelService.dictLabel("delivery_status", "IN_TRANSIT")).thenReturn("运输中");

        PageResult<DeliveryResponse> result = service.pageDeliveries(request);

        assertEquals(7, result.getTotal());
        assertEquals(2, result.getPageNum());
        assertEquals(5, result.getPageSize());
        DeliveryResponse row = result.getList().get(0);
        assertEquals("待配送", row.getLifecycleStageLabel());
        assertEquals("草稿", row.getStageStatusLabel());
        assertEquals("运输中", row.getDeliveryStatusLabel());
    }

    @Test
    void createDeliveryChecksPendingDeliveryAndStoresDraft() {
        doAnswer(invocation -> {
            VehDelivery delivery = invocation.getArgument(0);
            delivery.setId(51L);
            return 1;
        }).when(vehDeliveryMapper).insert(any(VehDelivery.class));

        Long id = service.createDelivery(50L, completeRequest());

        assertEquals(51L, id);
        verify(lifecycleService).assertStage(50L, LifecycleStage.PENDING_DELIVERY);
        ArgumentCaptor<VehDelivery> captor = ArgumentCaptor.forClass(VehDelivery.class);
        verify(vehDeliveryMapper).insert(captor.capture());
        VehDelivery saved = captor.getValue();
        assertEquals(50L, saved.getVehicleId());
        assertEquals(StageStatus.DRAFT.name(), saved.getStageStatus());
        assertEquals(LocalDate.of(2026, 7, 15), saved.getEtdToDealer());
        assertEquals(LocalDate.of(2026, 7, 16), saved.getEtaToDealer());
        assertEquals("4 units", saved.getTrollyType());
    }

    @Test
    void createDeliveryAllowsIncompleteDraft() {
        doAnswer(invocation -> {
            VehDelivery delivery = invocation.getArgument(0);
            delivery.setId(51L);
            return 1;
        }).when(vehDeliveryMapper).insert(any(VehDelivery.class));

        Long id = service.createDelivery(50L, new DeliverySaveRequest());

        assertEquals(51L, id);
        ArgumentCaptor<VehDelivery> captor = ArgumentCaptor.forClass(VehDelivery.class);
        verify(vehDeliveryMapper).insert(captor.capture());
        assertEquals(StageStatus.DRAFT.name(), captor.getValue().getStageStatus());
        assertNull(captor.getValue().getReceivedDate());
    }

    @Test
    void emptyDeliveryDraftPassesBeanValidation() {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            assertTrue(validator.validate(new DeliverySaveRequest()).isEmpty());
        }
    }

    @Test
    void createDeliveryRejectsDuplicateRecord() {
        when(vehDeliveryMapper.selectOne(any())).thenReturn(draftDelivery());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.createDelivery(50L, completeRequest()));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(vehDeliveryMapper, never()).insert(any(VehDelivery.class));
    }

    @Test
    void createDeliveryMapsUniqueKeyRaceToBadRequest() {
        when(vehDeliveryMapper.insert(any(VehDelivery.class)))
                .thenThrow(new DuplicateKeyException("uk_vehicle_id"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.createDelivery(50L, completeRequest()));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
    }

    @Test
    void saveRejectsEtaBeforeEtd() {
        DeliverySaveRequest request = new DeliverySaveRequest();
        request.setEtdToDealer(LocalDate.of(2026, 7, 16));
        request.setEtaToDealer(LocalDate.of(2026, 7, 15));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.createDelivery(50L, request));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(vehDeliveryMapper, never()).insert(any(VehDelivery.class));
    }

    @Test
    void saveRejectsUnsupportedTrollyType() {
        DeliverySaveRequest request = new DeliverySaveRequest();
        request.setTrollyType("8 units");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.createDelivery(50L, request));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(vehDeliveryMapper, never()).insert(any(VehDelivery.class));
    }

    @Test
    void updateDeliveryChecksDraftStatusAndCurrentLifecycleStage() {
        VehDelivery delivery = draftDelivery();
        when(vehDeliveryMapper.selectByVehicleIdForUpdate(50L)).thenReturn(delivery);
        DeliverySaveRequest request = completeRequest();
        request.setRemark7("updated");

        service.updateDelivery(50L, request);

        verify(lifecycleService).assertNotConfirmed(StageStatus.DRAFT.name());
        verify(lifecycleService).assertStage(50L, LifecycleStage.PENDING_DELIVERY);
        verify(vehDeliveryMapper).selectByVehicleIdForUpdate(50L);
        verify(vehDeliveryMapper).updateById(delivery);
        verify(vehDeliveryMapper, never()).selectOne(any());
        assertEquals("updated", delivery.getRemark7());
    }

    @Test
    void updateDeliveryRejectsConfirmedRecordBeforeCheckingLifecycle() {
        VehDelivery delivery = draftDelivery();
        delivery.setStageStatus(StageStatus.CONFIRMED.name());
        when(vehDeliveryMapper.selectByVehicleIdForUpdate(50L)).thenReturn(delivery);
        doThrow(new BusinessException(ErrorCode.STAGE_ALREADY_CONFIRMED))
                .when(lifecycleService).assertNotConfirmed(StageStatus.CONFIRMED.name());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateDelivery(50L, completeRequest()));

        assertEquals(ErrorCode.STAGE_ALREADY_CONFIRMED.getCode(), ex.getCode());
        verify(lifecycleService, never()).assertStage(any(), any());
        verify(vehDeliveryMapper, never()).updateById(any(VehDelivery.class));
    }

    @Test
    void updateDeliveryRejectsLifecycleStageMismatch() {
        when(vehDeliveryMapper.selectByVehicleIdForUpdate(50L)).thenReturn(draftDelivery());
        doThrow(new BusinessException(ErrorCode.LIFECYCLE_STAGE_MISMATCH))
                .when(lifecycleService).assertStage(50L, LifecycleStage.PENDING_DELIVERY);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateDelivery(50L, completeRequest()));

        assertEquals(ErrorCode.LIFECYCLE_STAGE_MISMATCH.getCode(), ex.getCode());
        verify(vehDeliveryMapper, never()).updateById(any(VehDelivery.class));
    }

    @Test
    void confirmRejectsMissingReceivedDate() {
        VehDelivery delivery = draftDelivery();
        delivery.setReceivedDate(null);
        when(vehDeliveryMapper.selectByVehicleIdForUpdate(50L)).thenReturn(delivery);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.confirmDelivery(50L));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(lifecycleService).assertNotConfirmed(StageStatus.DRAFT.name());
        verify(vehAllocationMapper, never()).selectOne(any());
        verify(lifecycleService, never()).confirmAndAdvance(any(), any(), any(), any(), any());
    }

    @Test
    void confirmRejectsPersistedEtaBeforeEtdBeforeAllocationLookup() {
        VehDelivery delivery = draftDelivery();
        delivery.setEtdToDealer(LocalDate.of(2026, 7, 16));
        delivery.setEtaToDealer(LocalDate.of(2026, 7, 15));
        when(vehDeliveryMapper.selectByVehicleIdForUpdate(50L)).thenReturn(delivery);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.confirmDelivery(50L));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(vehAllocationMapper, never()).selectOne(any());
        verify(lifecycleService, never()).confirmAndAdvance(any(), any(), any(), any(), any());
        verify(vehDeliveryMapper, never()).updateById(any(VehDelivery.class));
    }

    @Test
    void confirmRejectsPersistedUnsupportedTrollyTypeBeforeAllocationLookup() {
        VehDelivery delivery = draftDelivery();
        delivery.setTrollyType("8 units");
        when(vehDeliveryMapper.selectByVehicleIdForUpdate(50L)).thenReturn(delivery);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.confirmDelivery(50L));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(vehAllocationMapper, never()).selectOne(any());
        verify(lifecycleService, never()).confirmAndAdvance(any(), any(), any(), any(), any());
        verify(vehDeliveryMapper, never()).updateById(any(VehDelivery.class));
    }

    @Test
    void confirmRejectsVehicleWithoutConfirmedDealerAllocation() {
        when(vehDeliveryMapper.selectByVehicleIdForUpdate(50L)).thenReturn(draftDelivery());
        when(vehAllocationMapper.selectOne(any())).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.confirmDelivery(50L));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(lifecycleService, never()).confirmAndAdvance(any(), any(), any(), any(), any());
    }

    @Test
    void confirmRejectsAllocationWithoutDealer() {
        VehAllocation allocation = new VehAllocation();
        allocation.setStageStatus(StageStatus.CONFIRMED.name());
        when(vehDeliveryMapper.selectByVehicleIdForUpdate(50L)).thenReturn(draftDelivery());
        when(vehAllocationMapper.selectOne(any())).thenReturn(allocation);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.confirmDelivery(50L));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(lifecycleService, never()).confirmAndAdvance(any(), any(), any(), any(), any());
    }

    @Test
    void confirmRejectsRepeatedConfirmationWithoutAdvancing() {
        VehDelivery delivery = draftDelivery();
        delivery.setStageStatus(StageStatus.CONFIRMED.name());
        when(vehDeliveryMapper.selectByVehicleIdForUpdate(50L)).thenReturn(delivery);
        doThrow(new BusinessException(ErrorCode.STAGE_ALREADY_CONFIRMED))
                .when(lifecycleService).assertNotConfirmed(StageStatus.CONFIRMED.name());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.confirmDelivery(50L));

        assertEquals(ErrorCode.STAGE_ALREADY_CONFIRMED.getCode(), ex.getCode());
        verify(vehAllocationMapper, never()).selectOne(any());
        verify(lifecycleService, never()).confirmAndAdvance(any(), any(), any(), any(), any());
    }

    @Test
    void confirmPropagatesLifecycleStageMismatchWithoutLockingDelivery() {
        VehDelivery delivery = draftDelivery();
        VehAllocation allocation = confirmedAllocation();
        when(vehDeliveryMapper.selectByVehicleIdForUpdate(50L)).thenReturn(delivery);
        when(vehAllocationMapper.selectOne(any())).thenReturn(allocation);
        doThrow(new BusinessException(ErrorCode.LIFECYCLE_STAGE_MISMATCH))
                .when(lifecycleService).confirmAndAdvance(
                        eq(50L), eq(StageStatus.DRAFT.name()), eq(LifecycleStage.PENDING_DELIVERY),
                        eq(LifecycleStage.PENDING_REGISTRATION), any(Runnable.class));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.confirmDelivery(50L));

        assertEquals(ErrorCode.LIFECYCLE_STAGE_MISMATCH.getCode(), ex.getCode());
        assertEquals(StageStatus.DRAFT.name(), delivery.getStageStatus());
        verify(vehDeliveryMapper, never()).updateById(any(VehDelivery.class));
    }

    @Test
    void confirmQueriesConfirmedUndeletedAllocationForVehicle() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), VehAllocation.class);
        when(vehDeliveryMapper.selectByVehicleIdForUpdate(50L)).thenReturn(draftDelivery());
        when(vehAllocationMapper.selectOne(any())).thenReturn(confirmedAllocation());

        service.confirmDelivery(50L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<LambdaQueryWrapper<VehAllocation>> captor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(vehAllocationMapper).selectOne(captor.capture());
        LambdaQueryWrapper<VehAllocation> wrapper = captor.getValue();
        String sql = wrapper.getSqlSegment().toLowerCase(Locale.ROOT);
        assertTrue(sql.contains("vehicle_id"));
        assertTrue(sql.contains("stage_status"));
        assertTrue(sql.contains("deleted"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue(50L));
        assertTrue(wrapper.getParamNameValuePairs().containsValue(StageStatus.CONFIRMED.name()));
        assertTrue(wrapper.getParamNameValuePairs().containsValue(0));
    }

    @Test
    void confirmDeliveryDeclaresTransactionalBoundary() throws NoSuchMethodException {
        Method method = VehDeliveryServiceImpl.class.getMethod("confirmDelivery", Long.class);

        Transactional transactional = method.getAnnotation(Transactional.class);

        assertNotNull(transactional);
    }

    @Test
    void confirmDeliveryLocksAndAdvancesToRegistration() {
        VehDelivery delivery = draftDelivery();
        VehAllocation allocation = new VehAllocation();
        allocation.setDealerId(500L);
        allocation.setStageStatus(StageStatus.CONFIRMED.name());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("delivery-operator", null, Collections.emptyList()));
        when(vehDeliveryMapper.selectByVehicleIdForUpdate(50L)).thenReturn(delivery);
        when(vehAllocationMapper.selectOne(any())).thenReturn(allocation);
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(4).run();
            return null;
        }).when(lifecycleService).confirmAndAdvance(
                eq(50L), eq(StageStatus.DRAFT.name()), eq(LifecycleStage.PENDING_DELIVERY),
                eq(LifecycleStage.PENDING_REGISTRATION), any(Runnable.class));

        service.confirmDelivery(50L);

        assertEquals(StageStatus.CONFIRMED.name(), delivery.getStageStatus());
        assertEquals("delivery-operator", delivery.getConfirmedBy());
        assertNotNull(delivery.getConfirmedAt());
        verify(vehDeliveryMapper).selectByVehicleIdForUpdate(50L);
        verify(vehDeliveryMapper).updateById(delivery);
        verify(vehDeliveryMapper, never()).selectOne(any());
    }

    @Test
    void confirmDeliveryRollsBackDeliveryAndLifecycleRowsWhenAdvanceFails() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:delivery_tx_" + System.nanoTime()
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE TABLE t_veh_delivery (vehicle_id BIGINT PRIMARY KEY, stage_status VARCHAR(32))");
        jdbcTemplate.execute("CREATE TABLE t_vehicle (id BIGINT PRIMARY KEY, lifecycle_stage VARCHAR(64))");
        jdbcTemplate.update("INSERT INTO t_veh_delivery (vehicle_id, stage_status) VALUES (?, ?)",
                50L, StageStatus.DRAFT.name());
        jdbcTemplate.update("INSERT INTO t_vehicle (id, lifecycle_stage) VALUES (?, ?)",
                50L, LifecycleStage.PENDING_DELIVERY.name());

        VehDelivery delivery = draftDelivery();
        when(vehDeliveryMapper.selectByVehicleIdForUpdate(50L)).thenReturn(delivery);
        when(vehAllocationMapper.selectOne(any())).thenReturn(confirmedAllocation());
        when(vehDeliveryMapper.updateById(any(VehDelivery.class))).thenAnswer(invocation -> {
            VehDelivery updated = invocation.getArgument(0);
            return jdbcTemplate.update(
                    "UPDATE t_veh_delivery SET stage_status = ? WHERE vehicle_id = ?",
                    updated.getStageStatus(), updated.getVehicleId());
        });
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(4).run();
            jdbcTemplate.update("UPDATE t_vehicle SET lifecycle_stage = ? WHERE id = ?",
                    LifecycleStage.PENDING_REGISTRATION.name(), 50L);
            throw new RuntimeException("simulated lifecycle advance failure");
        }).when(lifecycleService).confirmAndAdvance(
                eq(50L), eq(StageStatus.DRAFT.name()), eq(LifecycleStage.PENDING_DELIVERY),
                eq(LifecycleStage.PENDING_REGISTRATION), any(Runnable.class));
        TransactionTemplate transactionTemplate = new TransactionTemplate(
                new DataSourceTransactionManager(dataSource));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> transactionTemplate.executeWithoutResult(status -> service.confirmDelivery(50L)));

        assertEquals("simulated lifecycle advance failure", ex.getMessage());
        assertEquals(StageStatus.DRAFT.name(), jdbcTemplate.queryForObject(
                "SELECT stage_status FROM t_veh_delivery WHERE vehicle_id = 50", String.class));
        assertEquals(LifecycleStage.PENDING_DELIVERY.name(), jdbcTemplate.queryForObject(
                "SELECT lifecycle_stage FROM t_vehicle WHERE id = 50", String.class));
    }

    @Test
    void getDeliveryReturnsDeliveryStatusLabel() {
        DeliveryResponse response = new DeliveryResponse();
        response.setId(51L);
        response.setVehicleId(50L);
        response.setLifecycleStage(LifecycleStage.PENDING_DELIVERY.name());
        response.setStageStatus(StageStatus.DRAFT.name());
        response.setDeliveryStatus("DELIVERED");
        when(vehDeliveryMapper.selectDeliveryByVehicleId(50L)).thenReturn(response);
        when(statusLabelService.lifecycleStageLabel(LifecycleStage.PENDING_DELIVERY.name())).thenReturn("待配送");
        when(statusLabelService.stageStatusLabel(StageStatus.DRAFT.name())).thenReturn("草稿");
        when(statusLabelService.dictLabel("delivery_status", "DELIVERED")).thenReturn("已送达");

        DeliveryResponse result = service.getDelivery(50L);

        assertEquals("待配送", result.getLifecycleStageLabel());
        assertEquals("草稿", result.getStageStatusLabel());
        assertEquals("已送达", result.getDeliveryStatusLabel());
    }

    @Test
    void getDeliveryReturnsPendingVehicleWithoutDraft() {
        DeliveryResponse response = new DeliveryResponse();
        response.setVehicleId(50L);
        response.setLifecycleStage(LifecycleStage.PENDING_DELIVERY.name());
        response.setStageStatus(LifecycleStage.PENDING_DELIVERY.name());
        when(vehDeliveryMapper.selectDeliveryByVehicleId(50L)).thenReturn(response);
        when(statusLabelService.lifecycleStageLabel(LifecycleStage.PENDING_DELIVERY.name())).thenReturn("待配送");
        when(statusLabelService.stageStatusLabel(LifecycleStage.PENDING_DELIVERY.name())).thenReturn("待配送");

        DeliveryResponse result = service.getDelivery(50L);

        assertNull(result.getId());
        assertEquals("待配送", result.getLifecycleStageLabel());
        assertEquals("待配送", result.getStageStatusLabel());
    }

    @Test
    void getDeliveryRejectsVehicleOutsidePendingDeliveryWithoutDraft() {
        DeliveryResponse response = new DeliveryResponse();
        response.setVehicleId(50L);
        response.setLifecycleStage(LifecycleStage.PENDING_REGISTRATION.name());
        when(vehDeliveryMapper.selectDeliveryByVehicleId(50L)).thenReturn(response);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.getDelivery(50L));

        assertEquals(ErrorCode.STAGE_DATA_NOT_FOUND.getCode(), ex.getCode());
    }

    private DeliverySaveRequest completeRequest() {
        DeliverySaveRequest request = new DeliverySaveRequest();
        request.setEtdToDealer(LocalDate.of(2026, 7, 15));
        request.setEtaToDealer(LocalDate.of(2026, 7, 16));
        request.setTrollyType("4 units");
        request.setFullyLoad(true);
        request.setReceivedDate(LocalDate.of(2026, 7, 16));
        request.setDeliveryStatus("DELIVERED");
        request.setRemark7("remark");
        return request;
    }

    private VehDelivery draftDelivery() {
        VehDelivery delivery = new VehDelivery();
        delivery.setId(51L);
        delivery.setVehicleId(50L);
        delivery.setStageStatus(StageStatus.DRAFT.name());
        delivery.setReceivedDate(LocalDate.of(2026, 7, 16));
        return delivery;
    }

    private VehAllocation confirmedAllocation() {
        VehAllocation allocation = new VehAllocation();
        allocation.setVehicleId(50L);
        allocation.setDealerId(500L);
        allocation.setStageStatus(StageStatus.CONFIRMED.name());
        allocation.setDeleted(0);
        return allocation;
    }
}
