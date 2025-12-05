package com.universe.life.user.privacy.domain.dto;

import lombok.Data;

/**
 * @author 毛伟然
 * @since 2025/12/5 14:41
 */
@Data
public class AdminUserRoleListDTO {
    private Long userId;

    private Long roleId;

    private String roleName;
}
