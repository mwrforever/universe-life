package com.universe.life.auth.service.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 平台免责声明VO
 *
 * @author 毛伟然
 * @since 2025/11/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "平台免责声明")
public class DisclaimerVO {

    @Schema(description = "平台免责声明内容")
    private String content;
}