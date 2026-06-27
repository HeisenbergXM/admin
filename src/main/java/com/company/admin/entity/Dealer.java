package com.company.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 经销商
 */
@Data
@TableName("t_md_dealer")
public class Dealer {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 经销商编码 */
    private String dealerCode;

    /** 经销商名称 */
    private String dealerName;

    /** 状态：0停用 1正常 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}