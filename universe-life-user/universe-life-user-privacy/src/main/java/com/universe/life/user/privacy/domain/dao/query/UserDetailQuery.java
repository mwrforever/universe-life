package com.universe.life.user.privacy.domain.dao.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户详情查询对象
 *
 * @author Claude
 * @since 2025-12-03
 */
@Data
@Schema(description = "用户详情查询对象")
public class UserDetailQuery {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "省份", example = "广东省")
    private String province;

    @Schema(description = "城市", example = "深圳市")
    private String city;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;
}