package com.universe.life.user.privacy.domain.dto.request;

import com.universe.life.user.privacy.enums.CommonStatus;
import com.universe.life.user.privacy.enums.DataScope;
import com.universe.life.user.privacy.enums.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色创建请求
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Data
@Schema(description = "角色创建请求")
public class RoleCreateRequest {

    @Schema(description = "角色编码（唯一标识）", example = "admin")
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过50")
    private String roleCode;

    @Schema(description = "角色名称", example = "管理员")
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50")
    private String roleName;

    @Schema(description = "角色类型：0 系统角色 1 业务角色 2 自定义角色", example = "0")
    private RoleType roleType;

    @Schema(description = "数据权限范围：0 全部 1 本部门 2 本部门及下级 3 仅自己", example = "0")
    private DataScope dataScope;

    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "状态：0 禁用 1 启用", example = "1")
    private CommonStatus status;

    @Schema(description = "角色描述")
    @Size(max = 500, message = "描述长度不能超过500")
    private String description;
}
