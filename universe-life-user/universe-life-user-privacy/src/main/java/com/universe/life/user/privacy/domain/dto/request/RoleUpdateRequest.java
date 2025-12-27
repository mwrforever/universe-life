package com.universe.life.user.privacy.domain.dto.request;

import com.universe.life.user.privacy.enums.DataScope;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色更新请求
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Data
@Schema(description = "角色更新请求")
public class RoleUpdateRequest {

    @Schema(description = "角色名称", example = "超级管理员")
    @Size(max = 50, message = "角色名称长度不能超过50")
    private String roleName;

    @Schema(description = "数据权限范围：0 全部 1 本部门 2 本部门及下级 3 仅自己", example = "0")
    private DataScope dataScope;

    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "角色描述")
    @Size(max = 500, message = "描述长度不能超过500")
    private String description;
}
