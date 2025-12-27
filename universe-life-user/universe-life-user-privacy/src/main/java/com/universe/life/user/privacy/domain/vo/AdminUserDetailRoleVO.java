package com.universe.life.user.privacy.domain.vo;

import com.universe.life.user.privacy.enums.DataScope;
import com.universe.life.user.privacy.enums.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员查询用户详情中的角色视图对象
 * 对应接口：GET /admin/{id} 中的roles字段
 *
 * @author Claude
 * @since 2025-12-03
 */
@Data
@Schema(description = "管理员查询用户详情中的角色视图对象")
public class AdminUserDetailRoleVO {

    @Schema(description = "角色ID", example = "1")
    private Long id;

    @Schema(description = "角色编码", example = "USER")
    private String roleCode;

    @Schema(description = "角色名称", example = "普通用户")
    private String roleName;

    @Schema(description = "角色类型", example = "BUSINESS")
    private RoleType roleType;

    @Schema(description = "数据权限范围", example = "SELF")
    private DataScope dataScope;
}