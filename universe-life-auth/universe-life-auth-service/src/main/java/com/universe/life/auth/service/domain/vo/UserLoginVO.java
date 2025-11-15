package com.universe.life.auth.service.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 毛伟然
 * @since 2025/11/13 16:26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户登录返回信息")
public class UserLoginVO {

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "刷新令牌")
    private String refreshToken;

    @Schema(description = "令牌过期时间")
    private String expiresIn;

    @Schema(description = "令牌类型")
    private String tokenType;

}
