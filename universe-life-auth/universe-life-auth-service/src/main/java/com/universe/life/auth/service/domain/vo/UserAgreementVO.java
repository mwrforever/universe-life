package com.universe.life.auth.service.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户服务协议VO
 *
 * @author 毛伟然
 * @since 2025/11/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户服务协议")
public class UserAgreementVO {

    @Schema(description = "用户服务协议内容")
    private String content;
}