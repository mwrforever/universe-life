package com.universe.life.user.privacy.domain.dto.request;

import com.universe.life.model.enums.UserStatus;
import com.universe.life.user.privacy.domain.vo.AdminUserRoleVO;
import com.universe.life.user.privacy.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户更新请求对象
 *
 * @author Claude
 * @since 2025-12-02
 */
@Data
@Schema(description = "用户更新请求对象")
public class UserUpdateRequest {

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