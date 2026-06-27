package com.company.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车型数据
 */
@Data
@TableName("t_md_model")
public class VehicleModel {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 物料编码 */
    private String materialCode;

    /** 车系 */
    private String series;

    /** 配置规格 */
    private String spec;

    /** 车型名称 */
    private String modelName;

    /** 车型代码 */
    private String modelCode;

    /** 年款 */
    private String yearMake;

    /** 状态：0停用 1正常 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}