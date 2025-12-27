package com.universe.life.user.privacy.domain.dto;

import com.universe.life.user.privacy.enums.DataScope;
import com.universe.life.user.privacy.enums.RoleType;
import lombok.Data;

/**
 * 管理员查询用户详情中的角色DTO
 * 用于连表查询角色信息
 *
 * @author Claude
 * @since 2025-12-03
 */
@Data
public class AdminUserDetailRoleDTO {

    private Long id;
    private String roleCode;
    private String roleName;
    private RoleType roleType;
    private DataScope dataScope;
}