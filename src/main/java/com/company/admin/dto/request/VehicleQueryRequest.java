package com.company.admin.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 车辆通用分页查询请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleQueryRequest extends PageRequest {

    private String vin;
    private Long modelId;
    private String modelName;
    private String series;
    private String lifecycleStage;
    private Long dealerId;
}
