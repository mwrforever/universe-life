package com.universe.life.user.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 平台员工密码重置请求
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Data
@Schema(description = "平台员工密码重置请求")
public class SysUserPasswordResetRequest {

    @Schema(description = "新密码", example = "123456")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在6-32之间")
    private String newPassword;
}
