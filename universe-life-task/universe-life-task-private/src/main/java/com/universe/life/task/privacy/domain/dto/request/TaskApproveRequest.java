package com.universe.life.task.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 审核通过请求
 */
@Data
@Schema(description = "审核通过请求")
public class TaskApproveRequest {

    @Schema(description = "备注", example = "审核通过")
    private String remark;
}
