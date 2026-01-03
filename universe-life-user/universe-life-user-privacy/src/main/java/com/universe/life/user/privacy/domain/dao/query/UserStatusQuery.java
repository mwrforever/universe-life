package com.universe.life.user.privacy.domain.dao.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 获取用户状态查询对象
 * 对应接口：GET /admin/status
 *
 * @author Claude
 * @since 2025-12-03
 */
@Data
@Schema(description = "获取用户状态查询对象")
public class UserStatusQuery {

    @Schema(description = "用户名", example = "testuser")
    @NotBlank(message = "用户名不能为空")
    private String username;
}