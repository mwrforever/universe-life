package com.universe.life.user.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量资源角色分配请求
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Data
@Schema(description = "批量资源角色分配请求")
public class ResourceRoleBatchAssignRequest {

    @Schema(description = "角色ID列表", example = "[1, 2, 3]")
    @NotEmpty(message = "角色ID列表不能为空")
    private List<Long> roleIds;

    @Schema(description = "资源ID", example = "5")
    @NotNull(message = "资源ID不能为空")
    private Long resourceId;
}
