package com.universe.life.user.privacy.domain.dao.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户查询对象
 *
 * @author Claude
 * @since 2025-12-02
 */
@Data
@Schema(description = "用户查询对象")
public class UserQuery {

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;

    @Schema(description = "用户名模糊查询", example = "test")
    private String username;

    @Schema(description = "用户状态筛选", example = "0", allowableValues = {"0", "1", "2", "3"})
    private Integer status;

    @Schema(description = "性别筛选：0-SECRET(保密) 1-MALE(男) 2-FEMALE(女)", example = "1", allowableValues = {"0", "1", "2"})
    private Integer gender;

    @Schema(description = "创建开始时间", example = "2024-12-01T00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "创建结束时间", example = "2024-12-31T23:59:59")
    private LocalDateTime endTime;
}