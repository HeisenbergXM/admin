package com.company.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 内饰颜色
 */
@Data
@TableName("t_md_interior_color")
public class InteriorColor {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 颜色名称（英文） */
    private String colorName;

    /** 颜色名称（中文） */
    private String colorNameCn;

    /** 状态：0停用 1正常 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}