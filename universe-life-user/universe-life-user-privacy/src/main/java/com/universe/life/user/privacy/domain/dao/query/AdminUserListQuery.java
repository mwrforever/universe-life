package com.universe.life.user.privacy.domain.dao.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 管理员分页查询用户列表查询对象
 * 对应接口：GET /admin/list
 *
 * @author Claude
 * @since 2025-12-03
 */
@Data
@Schema(description = "管理员分页查询用户列表查询对象")
public class AdminUserListQuery {

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小不能小于1")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer size = 10;

    @Schema(description = "用户名模糊查询", example = "test")
    private String username;

    @Schema(description = "用户状态筛选", example = "0")
    private String status;

    @Schema(description = "性别筛选：0-SECRET(保密) 1-MALE(男) 2-FEMALE(女)", example = "1")
    private Integer gender;

    @Schema(description = "创建开始时间", example = "2024-12-01")
    private String startTime;

    @Schema(description = "创建结束时间", example = "2024-12-31")
    private String endTime;
}