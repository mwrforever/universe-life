package com.universe.life.model.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author 毛伟然
 * @since 2025/11/1 16:58
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(description = "验证码登录表单")
public class LoginFormCaptchaDTO extends LoginFormBaseDTO {

    @Schema(description = "验证码")
    private String captcha;

}
