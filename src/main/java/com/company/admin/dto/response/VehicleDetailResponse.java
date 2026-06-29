package com.company.admin.dto.response;

import lombok.Data;

/**
 * 车辆详情响应。
 */
@Data
public class VehicleDetailResponse {

    private VehicleBasicInfo basicInfo;
    private ProductionResponse production;
}
