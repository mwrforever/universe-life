package com.universe.life.auth.service.domain.dto.request;

import com.universe.life.common.server.model.domain.domain.enums.CaptchaUsageType;
import com.universe.life.model.enums.UserAuthType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 毛伟然
 * @since 2025/11/15 15:53
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户注册表单")
public class RegisterFormRequest {

    @Schema(description = "用户名")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{4,16}$", message = "用户名格式错误")
    private String username;

    @Schema(description = "密码")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String password;

    @Schema(description = "用户标识")
    @NotNull(message = "用户标识不能为空")
    private String identification;

    @Schema(description = "用户认证方式")
    @NotNull(message = "用户认证方式不能为空")
    private UserAuthType identificationType;

    @Schema(description = "验证码用途")
    @NotNull(message = "验证码用途不能为空")
    private CaptchaUsageType captchaUsageType;

    @Schema(description = "验证码请求唯一标识")
    @NotNull(message = "验证码请求唯一标识不能为空")
    private String issuer;
}
