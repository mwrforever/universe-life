package com.universe.life.pay.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pay_record")
public class PayRecordPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("biz_type")
    private String bizType;

    @TableField("biz_id")
    private Long bizId;

    @TableField("request_no")
    private String requestNo;

    @TableField("payer_id")
    private Long payerId;

    @TableField("payee_id")
    private Long payeeId;

    @TableField("amount")
    private Long amount;

    @TableField("channel")
    private Integer channel;

    @TableField("status")
    private Integer status;

    @TableField("third_trade_no")
    private String thirdTradeNo;

    @TableField("third_prepay_id")
    private String thirdPrepayId;

    @TableField("paid_at")
    private LocalDateTime paidAt;

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
