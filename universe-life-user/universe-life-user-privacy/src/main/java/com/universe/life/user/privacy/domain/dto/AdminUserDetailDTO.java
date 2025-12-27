package com.universe.life.user.privacy.domain.dto;

import com.universe.life.user.privacy.enums.Gender;
import com.universe.life.model.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员查询用户详情DTO（包含角色信息）
 * 用于连表查询用户详情和角色信息
 *
 * @author Claude
 * @since 2025-12-03
 */
@Data
public class AdminUserDetailDTO {

    private Long id;
    private String username;
    private String avatarUrl;
    private Gender gender;
    private UserStatus status;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联的角色列表
    private List<AdminUserDetailRoleDTO> roles;
}