package com.universe.life.user.privacy.domain.dto.request;

import com.universe.life.model.enums.UserAuthType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 用户认证创建请求对象
 *
 * @author Claude
 * @since 2025-12-02
 */
@Data
@Schema(description = "用户认证创建请求对象")
public class UserAuthCreateRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @NotNull(message = "认证类型不能为空")
    @Schema(description = "认证类型：0-WECHAT(微信) 1-QQ(QQ) 2-ALIPAY(支付宝) 3-WEIBO(微博) 4-USERNAME(用户名) 5-PHONE(手机号) 6-EMAIL(邮箱)",
           example = "5", allowableValues = {"0", "1", "2", "3", "4", "5", "6"})
    private UserAuthType identificationType;

    @NotBlank(message = "认证名不能为空")
    @Schema(description = "认证名（手机号）", example = "13800138000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String identification;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码（明文）", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "有效期（null表示永久）", example = "2025-12-31T23:59:59")
    private LocalDateTime expiresIn;
}