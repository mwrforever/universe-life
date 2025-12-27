package com.universe.life.user.privacy.domain.vo;

import com.universe.life.user.privacy.enums.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 资源简单VO
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Data
@Schema(description = "资源简单VO")
public class ResourceSimpleVO {

    @Schema(description = "资源ID")
    private Long id;

    @Schema(description = "资源编码")
    private String resourceCode;

    @Schema(description = "资源名称")
    private String resourceName;

    @Schema(description = "资源类型")
    private ResourceType resourceType;
}
