package com.universe.life.auth.resource.domain.dto.request;

import com.universe.life.common.enums.CaptchaUsageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author 毛伟然
 * @since 2025/11/20 15:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "验证码验证请求表单")
@EqualsAndHashCode(callSuper = false)
public class VerifyCodeFormRequest {

    @Schema(description = "用户名/手机号/邮箱")
    @NotNull(message = "用户名/手机号/邮箱不能为空")
    private String identification;


    @Schema(description = "校验码 相当于用户密码和验证码")
    @NotNull(message = "校验码不能为空")
    private String verifyCode;

    @Schema(description = "验证码使用类型", example = "1")
    @NotNull(message = "验证码使用类型不能为空")
    private CaptchaUsageType captchaUsageType;
}
