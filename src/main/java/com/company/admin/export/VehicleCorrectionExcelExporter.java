package com.company.admin.export;

import com.company.admin.dto.response.VehicleCorrectionListResponse;

import java.util.List;

public interface VehicleCorrectionExcelExporter {
    byte[] export(List<VehicleCorrectionListResponse> rows);
}
