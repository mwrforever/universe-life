package com.universe.life.user.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户详情更新请求对象
 *
 * @author Claude
 * @since 2025-12-02
 */
@Data
@Schema(description = "用户详情更新请求对象")
public class UserDetailUpdateRequest {

    @Schema(description = "简介", example = "这是用户的个人简介")
    private String bio;

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

    @Schema(description = "扩展字段（JSON格式）", example = "{\"preferences\":{\"theme\":\"light\"}}")
    private String ext;
}