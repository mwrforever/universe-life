package com.universe.life.message.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 消息同步请求
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
public class SyncMessageRequest {

    /**
     * 上次同步的序列号
     */
    @NotNull(message = "序列号不能为空")
    private Long lastSequence;

    /**
     * 最大同步数量
     */
    private Integer limit = 100;
}
