package com.universe.life.user.privacy.domain.dto.request;

import com.universe.life.model.enums.UserAuthType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 毛伟然
 * @since 2025/11/13 15:21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "注册表单")
public class RegisterFormDTO {

    @NotNull(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{4,20}$", message = "用户名格式错误")
    @Schema(description = "用户名")
    private String username;

    @NotNull(message = "密码不能为空")
    @Schema(description = "密码")
    private String password;

    @NotNull(message = "认证类型不能为空")
    @Schema(description = "认证类型")
    private UserAuthType identificationType;

    @NotNull(message = "验证实体不能为空")
    @Schema(description = "验证实体")
    private String identification;
}
