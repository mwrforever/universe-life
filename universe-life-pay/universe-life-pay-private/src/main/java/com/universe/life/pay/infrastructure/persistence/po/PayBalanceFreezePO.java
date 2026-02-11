package com.universe.life.pay.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pay_balance_freeze")
public class PayBalanceFreezePO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("freeze_no")
    private String freezeNo;

    @TableField("biz_type")
    private String bizType;

    @TableField("biz_id")
    private Long bizId;

    @TableField("request_no")
    private String requestNo;

    @TableField("payer_id")
    private Long payerId;

    @TableField("amount")
    private Long amount;

    @TableField("status")
    private Integer status;

    @TableField("expire_at")
    private LocalDateTime expireAt;

    @TableField("extra")
    private String extra;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @Version
    @TableField("version")
    private Long version;
}
