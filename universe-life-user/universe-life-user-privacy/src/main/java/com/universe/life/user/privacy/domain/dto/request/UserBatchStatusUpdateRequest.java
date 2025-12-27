package com.universe.life.user.privacy.domain.dto.request;

import com.universe.life.model.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量用户状态更新请求对象
 *
 * @author Claude
 * @since 2025-12-02
 */
@Data
@Schema(description = "批量用户状态更新请求对象")
public class UserBatchStatusUpdateRequest {

    @NotEmpty(message = "用户ID列表不能为空")
    @Schema(description = "用户ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> userIds;

    @NotNull(message = "用户状态不能为空")
    @Schema(description = "新状态", example = "DISABLE")
    private UserStatus status;
}