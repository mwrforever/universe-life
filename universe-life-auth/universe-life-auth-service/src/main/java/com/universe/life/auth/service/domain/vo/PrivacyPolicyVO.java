package com.universe.life.auth.service.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 隐私政策VO
 *
 * @author 毛伟然
 * @since 2025/11/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "隐私政策")
public class PrivacyPolicyVO {

    @Schema(description = "隐私政策内容")
    private String content;
}