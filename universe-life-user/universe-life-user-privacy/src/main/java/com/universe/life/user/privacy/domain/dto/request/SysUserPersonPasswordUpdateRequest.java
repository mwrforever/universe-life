package com.universe.life.user.privacy.domain.dto.request;

import com.universe.life.common.server.model.domain.domain.enums.CaptchaUsageType;
import com.universe.life.user.privacy.enums.VerificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 当前用户密码修改请求
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Data
@Schema(description = "当前用户密码修改请求")
public class SysUserPersonPasswordUpdateRequest {

    @Schema(description = "验证方式：0=原密码验证, 1=邮箱验证码, 2=手机验证码", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "验证方式不能为空")
    private VerificationType verificationType;

    @Schema(description = "当前密码（verificationType=0时必填）", example = "oldPassword123")
    private String currentPassword;

    @Schema(description = "验证码（verificationType=1或2时必填）", example = "123456")
    private String captcha;

    @Schema(description = "验证码用途类型（verificationType=1或2时必填）", example = "3")
    private CaptchaUsageType captchaUsageType;

    @Schema(description = "新密码", example = "newPassword456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在6-32之间")
    private String newPassword;
}
