package com.universe.life.user.privacy.domain.vo;

import com.universe.life.model.enums.UserStatus;
import com.universe.life.user.privacy.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息VO（用户端）
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Data
@Schema(description = "用户信息VO")
public class UserInfoVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "性别")
    private Gender gender;

    @Schema(description = "用户状态")
    private UserStatus status;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
