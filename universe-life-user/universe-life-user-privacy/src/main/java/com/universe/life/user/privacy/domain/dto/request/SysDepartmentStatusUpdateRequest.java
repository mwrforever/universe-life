package com.universe.life.user.privacy.domain.dto.request;

import com.universe.life.user.privacy.enums.CommonStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 部门状态更新请求
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Data
@Schema(description = "部门状态更新请求")
public class SysDepartmentStatusUpdateRequest {

    @Schema(description = "状态：0 禁用 1 启用", example = "1")
    @NotNull(message = "状态不能为空")
    private CommonStatus status;
}
