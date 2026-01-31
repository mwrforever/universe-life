package com.universe.life.user.privacy.domain.dto.request;

import com.universe.life.user.privacy.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 当前用户个人资料更新请求
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Data
@Schema(description = "当前用户个人资料更新请求")
public class SysUserPersonProfileUpdateRequest {

    @Schema(description = "用户名（登录名）", example = "zhangsan")
    @Size(min = 2, max = 32, message = "用户名长度需在2-32之间")
    private String username;

    @Schema(description = "真实姓名", example = "张三")
    @Size(max = 32, message = "真实姓名长度不能超过32")
    private String realName;

    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "头像URL（COS原始key，未签名）", example = "avatar/2025/01/xxx.jpg")
    private String avatarUrl;

    @Schema(description = "性别", example = "1")
    private Gender gender;
}
