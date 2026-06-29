package com.company.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * VIN 候选查询请求。
 */
@Data
@Schema(description = "VIN 候选查询请求")
public class VehicleCandidateRequest {

    @Schema(description = "生命周期阶段")
    private String stage;
    @Schema(description = "VIN 模糊查询关键字")
    private String vinPattern;
}
