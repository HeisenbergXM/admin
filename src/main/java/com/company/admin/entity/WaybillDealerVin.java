package com.company.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("t_waybill_dealer_vin")
@Schema(description = "经销商行 VIN")
public class WaybillDealerVin {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "经销商行 ID")
    private Long waybillDealerId;

    @Schema(description = "车辆 ID")
    private Long vehicleId;
}
