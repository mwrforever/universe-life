package com.universe.life.user.privacy.domain.dao.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户认证查询对象
 *
 * @author Claude
 * @since 2025-12-03
 */
@Data
@Schema(description = "用户认证查询对象")
public class UserAuthQuery {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "认证类型", example = "6", allowableValues = {"0", "1", "2", "3", "4", "5", "6"})
    private Integer identificationType;

    @Schema(description = "认证名（模糊查询）", example = "test@example.com")
    private String identification;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;
}