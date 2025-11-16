package com.universe.life.auth.resource.domain.dto.request;

import com.universe.life.model.enums.UserAuthType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 毛伟然
 * @since 2025/11/15 21:17
 */
@Schema(description = "验证码请求参数")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaptchaRequest {

    private String identification;

    private UserAuthType identificationType;

}
