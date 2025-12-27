package com.universe.life.user.privacy.domain.dto.request;

import com.universe.life.model.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 用户状态更新请求对象
 *
 * @author Claude
 * @since 2025-12-02
 */
@Data
@Schema(description = "用户状态更新请求对象")
public class UserStatusUpdateRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @NotNull(message = "用户状态不能为空")
    @Schema(description = "用户状态", example = "DISABLE")
    private UserStatus status;
}