package com.company.admin.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 生产阶段表。
 */
@Data
@TableName("t_veh_production")
@Schema(description = "生产阶段记录")
public class VehProduction {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "车辆 ID")
    private Long vehicleId;
    @Schema(description = "阶段记录状态：DRAFT=草稿，CONFIRMED=已确认")
    private String stageStatus;
    @Schema(description = "车型 ID")
    private Long modelId;
    @Schema(description = "外饰颜色 ID")
    private Long exteriorColorId;
    @Schema(description = "内饰颜色 ID")
    private Long interiorColorId;
    @Schema(description = "发动机号")
    private String engineNumber;
    @Schema(description = "年款")
    private String yearMake;
    @Schema(description = "物料信息")
    private String material;
    @Schema(description = "Shipment 信息")
    private String shipment;
    @Schema(description = "批次")
    private String batch;
    @Schema(description = "Offline EPMB 日期")
    private LocalDate offlineEpmbDate;
    @Schema(description = "EPMB OK 日期")
    private LocalDate epmbOkDate;
    @Schema(description = "生产阶段备注")
    private String remark1;
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
