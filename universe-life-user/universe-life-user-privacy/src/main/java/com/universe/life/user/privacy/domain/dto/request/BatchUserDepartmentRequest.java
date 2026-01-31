package com.universe.life.user.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量用户部门关联请求
 *
 * @author 毛伟然
 * @since 2026-01-17
 */
@Data
@Schema(description = "批量用户部门关联请求")
public class BatchUserDepartmentRequest {

    @Schema(description = "部门ID")
    @NotNull(message = "部门ID不能为空")
    private Long departmentId;

    @Schema(description = "用户ID列表")
    @NotEmpty(message = "用户ID列表不能为空")
    @Size(min = 1, max = 100, message = "批量操作数量必须在1-100之间")
    private List<Long> userIds;

    @Schema(description = "是否主部门")
    private Boolean isPrimary = false;
}
