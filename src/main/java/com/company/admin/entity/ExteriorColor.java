package com.company.admin.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 外饰颜色
 */
@Data
@TableName("t_md_exterior_color")
@Schema(description = "外饰颜色")
public class ExteriorColor {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键 ID")
    private Long id;

    /** 颜色名称（英文） */
    @Schema(description = "颜色名称")
    private String colorName;

    /** 颜色名称（中文） */
    @Schema(description = "颜色中文名称")
    private String colorNameCn;

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