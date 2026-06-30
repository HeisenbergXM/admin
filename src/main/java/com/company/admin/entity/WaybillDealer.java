package com.company.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_waybill_dealer")
@Schema(description = "行车路单经销商行")
public class WaybillDealer {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "行车路单 ID")
    private Long waybillId;

    @Schema(description = "经销商 ID")
    private Long dealerId;

    @Schema(description = "发车日期")
    private LocalDate etdToDealer;

    @Schema(description = "预计到达日期")
    private LocalDate etaToDealer;

    @Schema(description = "签收日期")
    private LocalDate receivedDate;

    @Schema(description = "配送状态")
    private String deliveryStatus;

    @Schema(description = "备注7")
    private String remark7;

    @Schema(description = "行状态")
    private String rowStatus;

    @Schema(description = "确认人")
    private String confirmedBy;

    @Schema(description = "确认时间")
    private LocalDateTime confirmedAt;
}
