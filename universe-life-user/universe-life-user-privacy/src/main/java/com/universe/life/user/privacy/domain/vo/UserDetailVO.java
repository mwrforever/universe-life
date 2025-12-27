package com.universe.life.user.privacy.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户详情视图对象
 *
 * @author Claude
 * @since 2025-12-03
 */
@Data
@Schema(description = "用户详情视图对象")
public class UserDetailVO {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "扩展字段（JSON格式）", example = "{\"preferences\":{\"theme\":\"dark\"}}")
    private String ext;

    @Schema(description = "用户简介", example = "这是用户简介")
    private String bio;

    @Schema(description = "接单数量", example = "10")
    private Integer receiveOrder;

    @Schema(description = "生日", example = "1990-01-01")
    private String birthday;

    @Schema(description = "省份", example = "广东省")
    private String province;

    @Schema(description = "城市", example = "深圳市")
    private String city;

    @Schema(description = "国家", example = "中国")
    private String country;

    @Schema(description = "街道地址", example = "科技园路")
    private String road;

    @Schema(description = "详细地址", example = "深圳市南山区科技园")
    private String address;

    @Schema(description = "创建时间", example = "2024-12-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2024-12-01T10:00:00")
    private LocalDateTime updatedAt;
}