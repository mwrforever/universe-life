package com.universe.life.auth.service.domain.dto.request;

import com.universe.life.model.enums.UserAuthType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "登录表单")
public class LoginFormRequest {

    @Schema(description = "用户名/手机号/邮箱")
    @NotNull
    private String identification;

    @Schema(description = "认证方式")
    @NotNull
    private UserAuthType authType;

    @Schema(description = "密码")
    @NotNull
    private String password;

}
