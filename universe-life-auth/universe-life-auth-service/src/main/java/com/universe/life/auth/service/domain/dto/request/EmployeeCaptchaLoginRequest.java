package com.universe.life.auth.service.domain.dto.request;

import com.universe.life.common.server.model.domain.domain.enums.CaptchaUsageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 员工验证码登录请求对象
 *
 * @author 毛伟然
 * @since 2025/12/29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "员工验证码登录请求")
public class EmployeeCaptchaLoginRequest {

    @Schema(description = "用户标识（用户名/手机号/邮箱）")
    @NotBlank(message = "用户标识不能为空")
    @Size(min = 4, max = 50, message = "用户标识长度必须在4-50之间")
    private String identification;

    @Schema(description = "验证码")
    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 8, message = "验证码长度必须在4-8之间")
    private String captcha;

    @Schema(description = "验证码使用类型")
    @NotNull(message = "验证码使用类型不能为空")
    private CaptchaUsageType captchaUsageType;
}
