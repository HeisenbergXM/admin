package com.company.admin.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 经销商
 */
@Data
@TableName("t_md_dealer")
@Schema(description = "经销商")
public class Dealer {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键 ID")
    private Long id;

    /** 经销商编码 */
    @Schema(description = "经销商编码")
    private String dealerCode;

    /** 经销商名称 */
    @Schema(description = "经销商名称")
    private String dealerName;

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