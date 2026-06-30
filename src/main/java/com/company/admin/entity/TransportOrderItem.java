package com.company.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("t_transport_order_item")
@Schema(description = "运输单 VIN 明细")
public class TransportOrderItem {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "运输单 ID")
    private Long transportOrderId;

    @Schema(description = "车辆 ID")
    private Long vehicleId;

    @Schema(description = "SAIC buy off 日期")
    private LocalDate saicBuyOffDate;
}
