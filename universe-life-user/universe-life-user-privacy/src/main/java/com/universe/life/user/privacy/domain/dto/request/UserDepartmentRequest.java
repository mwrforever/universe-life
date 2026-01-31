package com.universe.life.user.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户部门关联请求
 *
 * @author 毛伟然
 * @since 2026-01-17
 */
@Data
@Schema(description = "用户部门关联请求")
public class UserDepartmentRequest {

    @Schema(description = "用户ID")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "部门ID")
    @NotNull(message = "部门ID不能为空")
    private Long departmentId;

    @Schema(description = "是否主部门")
    private Boolean isPrimary = false;
}
