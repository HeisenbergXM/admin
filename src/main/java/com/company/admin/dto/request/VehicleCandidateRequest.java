package com.company.admin.dto.request;

import lombok.Data;

/**
 * VIN 候选查询请求。
 */
@Data
public class VehicleCandidateRequest {

    private String stage;
    private String vinPattern;
}
