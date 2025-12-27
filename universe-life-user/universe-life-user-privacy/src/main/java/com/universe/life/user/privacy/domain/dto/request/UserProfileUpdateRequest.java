package com.universe.life.user.privacy.domain.dto.request;

import com.universe.life.user.privacy.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户个人信息更新请求（用户端）
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Data
@Schema(description = "用户个人信息更新请求")
public class UserProfileUpdateRequest {

    @Schema(description = "用户名")
    @Size(min = 2, max = 32, message = "用户名长度必须在2-32个字符之间")
    private String username;

    @Schema(description = "头像URL")
    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatarUrl;

    @Schema(description = "性别")
    private Gender gender;
}
