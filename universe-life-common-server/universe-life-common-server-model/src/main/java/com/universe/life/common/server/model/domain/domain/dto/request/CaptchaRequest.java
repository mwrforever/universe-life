package com.universe.life.common.server.model.domain.domain.dto.request;

import com.universe.life.common.server.model.domain.domain.enums.CaptchaUsageType;
import com.universe.life.model.enums.UserAuthType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @Schema(description = "验证实体")
    @NotBlank(message = "验证实体不能为空")
    private String identification;

    @Schema(description = "验证方式")
    @NotNull(message = "验证方式不能为空")
    private UserAuthType identificationType;

    @Schema(description = "验证用途")
    @NotNull(message = "验证用途不能为空")
    private CaptchaUsageType captchaUsageType;

}
