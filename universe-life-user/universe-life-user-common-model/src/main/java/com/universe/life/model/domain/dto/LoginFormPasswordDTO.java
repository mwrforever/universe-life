package com.universe.life.model.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * @author 毛伟然
 * @since 2025/11/4 12:09
 */
@Schema(description = "密码登录校验表单")
@Data
@RequiredArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LoginFormPasswordDTO extends LoginFormBaseDTO {

    @Schema(description = "密码")
    private String password;

}
