package com.universe.life.user.privacy.domain.dto.request;

import com.universe.life.common.server.model.domain.domain.enums.CaptchaUsageType;
import com.universe.life.model.enums.UserAuthType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户认证更新请求对象
 *
 * @author Claude
 * @since 2025-12-02
 */
@Data
@Schema(description = "用户认证更新请求对象")
public class UserAuthUpdateRequest {

    @NotBlank(message = "原密码不能为空")
    @Schema(description = "原密码（明文）", example = "oldpassword123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String rowPassword;

    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码（明文）", example = "newpassword123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    @NotBlank(message = "验证码通过标识不能为空")
    @Schema(description = "验证码通过标识", example = "sgojgjg", requiredMode = Schema.RequiredMode.REQUIRED)
    private String issuer;

    @NotBlank(message = "认证名不能为空")
    @Schema(description = "认证名（邮箱）", example = "18475993424@163.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String identification;

    @NotNull(message = "验证码用途类型不能为空")
    @Schema(description = "验证码用途类型：1-LOGIN(登录) 2-REGISTER(注册) 3-RESET_PASSWORD(重置密码) 4-BIND_EMAIL(绑定邮箱) 5-UNBIND_EMAIL(解绑邮箱) 6-MODIFY_PAYMENT_PASSWORD(修改支付密码)",
           example = "3", allowableValues = {"1", "2", "3", "4", "5", "6"})
    private CaptchaUsageType captchaUsageType;

    @NotNull(message = "认证类型不能为空")
    @Schema(description = "认证类型：0-微信 1-QQ 2-支付宝 3-微博 4-用户名 5-手机号 6-邮箱",
           example = "6", allowableValues = {"0", "1", "2", "3", "4", "5", "6"})
    private UserAuthType identificationType;
}