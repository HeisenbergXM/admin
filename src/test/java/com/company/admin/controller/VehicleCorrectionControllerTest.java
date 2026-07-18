package com.company.admin.controller;

import com.company.admin.common.BusinessException;
import com.company.admin.common.ErrorCode;
import com.company.admin.common.PageResult;
import com.company.admin.dto.request.VehicleQueryRequest;
import com.company.admin.dto.response.VehicleCorrectionListResponse;
import com.company.admin.entity.OperationLog;
import com.company.admin.mapper.OperationLogMapper;
import com.company.admin.service.VehicleCorrectionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VehicleCorrectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleCorrectionService vehicleCorrectionService;

    @MockBean
    private OperationLogMapper operationLogMapper;

    @Test
    @WithMockUser(authorities = "vlm:vehicle-correction:list")
    void listPermissionCanRead() throws Exception {
        VehicleCorrectionListResponse response = new VehicleCorrectionListResponse();
        response.setVinNumber("VIN90");
        when(vehicleCorrectionService.pageCorrections(any()))
                .thenReturn(new PageResult<>(List.of(response), 1, 1, 10));

        mockMvc.perform(get("/api/vehicle-corrections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].vinNumber").value("VIN90"));
    }

    @Test
    @WithMockUser(authorities = "vlm:vehicle-correction:export")
    void exportPermissionDownloadsFilteredWorkbook() throws Exception {
        when(vehicleCorrectionService.exportCorrections(any())).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/api/vehicle-corrections/export")
                        .param("vin", "VIN90")
                        .param("pageNum", "9")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition",
                        matchesPattern("attachment;.*vehicle-corrections-\\d{14}\\.xlsx.*")))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));

        ArgumentCaptor<VehicleQueryRequest> captor = ArgumentCaptor.forClass(VehicleQueryRequest.class);
        verify(vehicleCorrectionService).exportCorrections(captor.capture());
        assertEquals("VIN90", captor.getValue().getVin());
    }

    @Test
    @WithMockUser(authorities = "vlm:vehicle-correction:list")
    void listPermissionCannotExport() throws Exception {
        mockMvc.perform(get("/api/vehicle-corrections/export"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN.getCode()));
        verify(vehicleCorrectionService, never()).exportCorrections(any());
    }

    @Test
    @WithMockUser(authorities = "vlm:vehicle-correction:list")
    void listPermissionCannotEdit() throws Exception {
        mockMvc.perform(put("/api/vehicle-corrections/90")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN.getCode()));

        verify(vehicleCorrectionService, never()).updateCorrection(any(), any());
    }

    @Test
    @WithMockUser(authorities = "vlm:vehicle-correction:edit")
    void editPermissionCanSave() throws Exception {
        mockMvc.perform(put("/api/vehicle-corrections/90")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(vehicleCorrectionService).updateCorrection(eq(90L), any());
    }

    @Test
    @WithMockUser(username = "correction-admin",
            authorities = "vlm:vehicle-correction:edit")
    void failedEditKeepsExistingOperationLogAudit() throws Exception {
        doThrow(new BusinessException(ErrorCode.BAD_REQUEST))
                .when(vehicleCorrectionService).updateCorrection(eq(90L), any());

        mockMvc.perform(put("/api/vehicle-corrections/90")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"payment\":{\"id\":701,\"paymentDate\":\"2026-07-18\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.getCode()));

        ArgumentCaptor<OperationLog> captor = ArgumentCaptor.forClass(OperationLog.class);
        verify(operationLogMapper).insert(captor.capture());
        assertEquals("correction-admin", captor.getValue().getUsername());
        assertEquals("车辆数据修订", captor.getValue().getOperation());
        assertEquals("VehicleCorrectionController.update", captor.getValue().getMethod());
        assertTrue(captor.getValue().getParams().contains("[90,"));
        assertTrue(captor.getValue().getParams().contains("\"id\":701"));
        assertTrue(captor.getValue().getParams().contains("\"paymentDate\":\"2026-07-18\""));
        assertTrue(captor.getValue().getResult().startsWith("异常:"));
    }
}
