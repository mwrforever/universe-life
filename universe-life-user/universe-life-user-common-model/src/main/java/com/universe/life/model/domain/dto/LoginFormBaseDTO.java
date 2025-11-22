package com.universe.life.model.domain.dto;

import com.universe.life.model.enums.UserAuthType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 毛伟然
 * @since 2025/11/4 11:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户登录基础表单")
public class LoginFormBaseDTO {

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
