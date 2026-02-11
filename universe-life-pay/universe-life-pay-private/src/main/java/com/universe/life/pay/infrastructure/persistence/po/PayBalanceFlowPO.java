package com.universe.life.pay.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pay_balance_flow")
public class PayBalanceFlowPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("flow_no")
    private String flowNo;

    @TableField("request_no")
    private String requestNo;

    @TableField("user_id")
    private Long userId;

    @TableField("biz_type")
    private String bizType;

    @TableField("biz_id")
    private Long bizId;

    @TableField("action")
    private String action;

    @TableField("amount")
    private Long amount;

    @TableField("available_delta")
    private Long availableDelta;

    @TableField("frozen_delta")
    private Long frozenDelta;

    @TableField("balance_after")
    private Long balanceAfter;

    @TableField("frozen_after")
    private Long frozenAfter;

    @TableField("remark")
    private String remark;

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
