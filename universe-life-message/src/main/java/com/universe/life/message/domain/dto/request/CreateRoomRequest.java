package com.universe.life.message.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建公共聊天室 Request
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建公共聊天室请求")
public class CreateRoomRequest {

    @NotBlank(message = "聊天室名称不能为空")
    @Size(max = 50, message = "聊天室名称不能超过50字")
    @Schema(description = "聊天室名称", required = true)
    private String name;

    @Schema(description = "聊天室头像URL")
    private String avatar;

    @Size(max = 500, message = "聊天室描述不能超过500字")
    @Schema(description = "聊天室描述")
    private String description;

    @Schema(description = "聊天室分类")
    private String category;

    @Schema(description = "最大在线人数", defaultValue = "1000")
    private Integer maxOnline;
}
