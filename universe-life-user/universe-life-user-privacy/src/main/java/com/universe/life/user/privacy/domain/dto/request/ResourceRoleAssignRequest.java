package com.universe.life.user.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 资源角色分配请求
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Data
@Schema(description = "资源角色分配请求")
public class ResourceRoleAssignRequest {

    @Schema(description = "角色ID", example = "1")
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    @Schema(description = "资源ID列表", example = "[1, 2, 3, 4, 5]")
    @NotEmpty(message = "资源ID列表不能为空")
    private List<Long> resourceIds;
}
