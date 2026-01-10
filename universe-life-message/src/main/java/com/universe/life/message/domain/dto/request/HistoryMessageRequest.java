package com.universe.life.message.domain.dto.request;

import com.universe.life.message.enums.MessageType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 历史消息查询请求
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
public class HistoryMessageRequest {

    /**
     * 消息类型
     */
    @NotNull(message = "消息类型不能为空")
    private MessageType messageType;

    /**
     * 目标ID（私聊为对方用户ID，群聊为群组ID，聊天室为聊天室ID）
     */
    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer pageSize = 20;
}
