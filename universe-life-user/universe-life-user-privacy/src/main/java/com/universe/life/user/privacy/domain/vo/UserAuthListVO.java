package com.universe.life.user.privacy.domain.vo;

import com.universe.life.model.enums.UserAuthType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户认证方式列表响应视图对象
 * 对应接口：GET /auth/list/{userId}
 *
 * @author Claude
 * @since 2025-12-03
 */
@Data
@Schema(description = "用户认证方式列表响应视图对象")
public class UserAuthListVO {

    @Schema(description = "认证ID", example = "1")
    private Long id;

    @Schema(description = "认证类型")
    private UserAuthType identificationType;

    @Schema(description = "认证名（账号）", example = "13800138000")
    private String identification;
}