package com.universe.life.user.privacy.domain.dto.request;

import com.universe.life.user.privacy.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 平台员工个人信息更新请求
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Data
@Schema(description = "平台员工个人信息更新请求")
public class SysUserProfileUpdateRequest {

    @Schema(description = "真实姓名")
    @Size(max = 32, message = "姓名长度不能超过32个字符")
    private String realName;

    @Schema(description = "手机号")
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    @Size(max = 64, message = "邮箱长度不能超过64个字符")
    private String email;

    @Schema(description = "头像URL")
    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatarUrl;

    @Schema(description = "性别")
    private Gender gender;
}
