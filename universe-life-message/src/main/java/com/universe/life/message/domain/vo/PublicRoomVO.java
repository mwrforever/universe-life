package com.universe.life.message.domain.vo;

import com.universe.life.message.enums.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 公共聊天室 VO
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公共聊天室信息")
public class PublicRoomVO {

    @Schema(description = "聊天室ID")
    private Long id;

    @Schema(description = "聊天室名称")
    private String name;

    @Schema(description = "聊天室头像")
    private String avatar;

    @Schema(description = "聊天室描述")
    private String description;

    @Schema(description = "聊天室分类")
    private String category;

    @Schema(description = "最大在线人数")
    private Integer maxOnline;

    @Schema(description = "当前在线人数")
    private Integer currentOnline;

    @Schema(description = "累计加入人数")
    private Integer totalMembers;

    @Schema(description = "状态")
    private RoomStatus status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
