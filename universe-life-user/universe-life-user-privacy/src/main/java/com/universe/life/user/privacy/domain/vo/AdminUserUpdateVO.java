package com.universe.life.user.privacy.domain.vo;

import com.universe.life.user.privacy.enums.Gender;
import com.universe.life.model.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 更新用户信息响应视图对象
 * 对应接口：PUT /admin/{id}
 *
 * @author Claude
 * @since 2025-12-03
 */
@Data
@Schema(description = "更新用户信息响应视图对象")
public class AdminUserUpdateVO {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "updateduser")
    private String username;

    @Schema(description = "头像URL", example = "https://example.com/new-avatar.jpg")
    private String avatarUrl;

    @Schema(description = "性别")
    private Gender gender;

    @Schema(description = "用户状态")
    private UserStatus status;

    @Schema(description = "最后登录时间", example = "2024-12-01T09:30:00")
    private LocalDateTime lastLoginAt;

    @Schema(description = "最后登录IP", example = "192.168.1.100")
    private String lastLoginIp;

    @Schema(description = "创建时间", example = "2024-12-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2024-12-01T11:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "角色列表")
    private List<AdminUserRoleVO> roles;
}