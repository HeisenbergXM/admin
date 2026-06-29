package com.company.admin.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车型数据
 */
@Data
@TableName("t_md_model")
@Schema(description = "车型数据")
public class VehicleModel {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键 ID")
    private Long id;

    /** 物料编码 */
    @Schema(description = "物料编码")
    private String materialCode;

    /** 车系 */
    @Schema(description = "车系")
    private String series;

    /** 配置规格 */
    @Schema(description = "配置规格")
    private String spec;

    /** 车型名称 */
    @Schema(description = "车型名称")
    private String modelName;

    /** 车型代码 */
    @Schema(description = "车型代码")
    private String modelCode;

    /** 年款 */
    @Schema(description = "年款")
    private String yearMake;

    /** 状态：0停用 1正常 */
    @Schema(description = "状态：0=停用，1=正常", example = "1")
    private Integer status;

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