package com.universe.life.common.server.model.domain.domain.dto.request;


import com.universe.life.model.enums.UserAuthType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 毛伟然
 * @since 2025/11/13 16:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "登录表单")
public class VerifyFormRequest {

    @Schema(description = "用户名/手机号/邮箱")
    @NotNull(message = "用户名/手机号/邮箱不能为空")
    private String identification;

    @Schema(description = "认证方式")
    @NotNull(message = "认证方式不能为空")
    private UserAuthType authType;

    @Schema(description = "校验码 相当于用户密码和验证码")
    @NotNull(message = "校验码不能为空")
    private String verifyCode;

}
