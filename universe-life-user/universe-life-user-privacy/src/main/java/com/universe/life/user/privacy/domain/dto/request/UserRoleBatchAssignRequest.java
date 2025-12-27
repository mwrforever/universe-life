package com.universe.life.user.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量用户角色分配请求
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Data
@Schema(description = "批量用户角色分配请求")
public class UserRoleBatchAssignRequest {

    @Schema(description = "用户ID列表", example = "[1, 2, 3]")
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;

    @Schema(description = "角色ID", example = "1")
    @NotNull(message = "角色ID不能为空")
    private Long roleId;
}
