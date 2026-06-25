package com.company.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车辆主表（精简主表，只存标识与生命周期）
 */
@Data
@TableName("t_vehicle")
public class Vehicle {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 车辆识别码（唯一） */
    private String vin;

    /** 当前生命周期阶段 */
    private String lifecycleStage;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
