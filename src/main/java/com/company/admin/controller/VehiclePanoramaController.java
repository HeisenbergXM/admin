package com.company.admin.controller;

import com.company.admin.common.Result;
import com.company.admin.dto.response.VehiclePanoramaResponse;
import com.company.admin.service.VehiclePanoramaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/panorama")
@RequiredArgsConstructor
@Tag(name = "车辆全景视图")
public class VehiclePanoramaController {

    private final VehiclePanoramaService vehiclePanoramaService;

    @GetMapping("/{vin}")
    @Operation(summary = "车辆全景视图")
    @PreAuthorize("hasAuthority('vlm:panorama:view')")
    public Result<VehiclePanoramaResponse> getPanorama(@Parameter(description = "VIN") @PathVariable String vin) {
        return Result.success(vehiclePanoramaService.getPanorama(vin));
    }

    @GetMapping("/{vin}/export")
    @Operation(summary = "导出车辆全景 Excel")
    @PreAuthorize("hasAuthority('vlm:panorama:export')")
    public ResponseEntity<byte[]> exportPanorama(@Parameter(description = "VIN") @PathVariable String vin) {
        byte[] bytes = vehiclePanoramaService.exportPanorama(vin);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("vehicle-panorama-" + vin + ".xlsx", StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}
