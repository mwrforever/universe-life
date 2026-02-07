package com.universe.life.aftercare.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("aftercare_appeal")
public class AftercareAppealPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private Long orderId;

    @TableField("task_id")
    private Long taskId;

    @TableField("appellant_id")
    private Long appellantId;

    @TableField("appeal_type")
    private Integer appealType;

    @TableField("reason")
    private String reason;

    @TableField("evidence_images")
    private String evidenceImages;

    @TableField("status")
    private Integer status;

    @TableField("result")
    private Integer result;

    @TableField("handler_id")
    private Long handlerId;

    @TableField("handle_remark")
    private String handleRemark;

    @TableField("handled_at")
    private LocalDateTime handledAt;

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
