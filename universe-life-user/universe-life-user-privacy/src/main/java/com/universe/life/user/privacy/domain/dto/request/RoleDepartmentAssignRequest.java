package com.universe.life.user.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 部门角色分配请求
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Data
@Schema(description = "部门角色分配请求")
public class RoleDepartmentAssignRequest {

    @Schema(description = "角色ID")
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    @Schema(description = "部门ID列表")
    @NotEmpty(message = "部门ID列表不能为空")
    private List<Long> departmentIds;
}
