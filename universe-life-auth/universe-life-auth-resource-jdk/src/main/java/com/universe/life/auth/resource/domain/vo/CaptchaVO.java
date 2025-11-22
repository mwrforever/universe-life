package com.universe.life.auth.resource.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 毛伟然
 * @since 2025/11/15 21:13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "验证码响应对象")
public class CaptchaVO {
    private String issuer;
}
