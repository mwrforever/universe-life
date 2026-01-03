package com.universe.life.model.domain.dto;

import com.universe.life.model.enums.UserAuthType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author 毛伟然
 * @since 2025/11/4 12:09
 */
@Schema(description = "密码登录校验表单")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LoginFormPasswordDTO {

    @Schema(description = "密码")
    private String password;

    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    private Boolean remember;

    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    private UserAuthType type;

}
