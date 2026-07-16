package com.company.admin.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_veh_delivery")
@Schema(description = "单车配送阶段记录")
public class VehDelivery {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "车辆 ID")
    private Long vehicleId;

    @Schema(description = "阶段记录状态：DRAFT=草稿，CONFIRMED=已确认")
    private String stageStatus;

    @Schema(description = "预计发往经销商日期")
    private LocalDate etdToDealer;

    @Schema(description = "预计到达经销商日期")
    private LocalDate etaToDealer;

    @Schema(description = "拖车类型")
    private String trollyType;

    @Schema(description = "是否满载")
    private Boolean fullyLoad;

    @Schema(description = "签收日期")
    private LocalDate receivedDate;

    @Schema(description = "配送状态")
    private String deliveryStatus;

    @Schema(description = "备注7")
    private String remark7;

    @Schema(description = "确认人")
    private String confirmedBy;

    @Schema(description = "确认时间")
    private LocalDateTime confirmedAt;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人")
    private String updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @TableLogic
    @Schema(description = "逻辑删除标记：0=未删除，1=已删除")
    private Integer deleted;
}
