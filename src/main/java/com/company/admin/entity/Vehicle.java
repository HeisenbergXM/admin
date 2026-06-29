package com.company.admin.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车辆主表（精简主表，只存标识与生命周期）
 */
@Data
@TableName("t_vehicle")
@Schema(description = "车辆主记录")
public class Vehicle {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键 ID")
    private Long id;

    /** 车辆识别码（唯一） */
    @Schema(description = "车辆识别码 VIN", example = "LSJW56U95RG000001")
    private String vin;

    /** 当前生命周期阶段 */
    @Schema(description = "当前生命周期阶段")
    private String lifecycleStage;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    private String createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人")
    private String updatedBy;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "逻辑删除标记：0=未删除，1=已删除")
    private Integer deleted;
}
