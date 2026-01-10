package com.universe.life.message.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 好友信息 VO
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "好友信息")
public class FriendVO {

    @Schema(description = "好友ID")
    private Long friendId;

    @Schema(description = "好友备注名")
    private String remark;

    @Schema(description = "好友昵称")
    private String nickname;

    @Schema(description = "好友头像")
    private String avatar;

    @Schema(description = "是否在线")
    private Boolean online;

    @Schema(description = "成为好友时间")
    private LocalDateTime createdAt;
}
