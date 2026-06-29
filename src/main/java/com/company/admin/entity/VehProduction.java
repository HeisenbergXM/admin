package com.company.admin.entity;

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
public class VehProduction {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long vehicleId;
    private String stageStatus;
    private Long modelId;
    private Long exteriorColorId;
    private Long interiorColorId;
    private String engineNumber;
    private String yearMake;
    private String material;
    private String shipment;
    private String batch;
    private LocalDate offlineEpmbDate;
    private LocalDate epmbOkDate;
    private String remark1;
    private String confirmedBy;
    private LocalDateTime confirmedAt;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
