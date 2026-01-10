package com.universe.life.trade.privacy.domain.dto;

import com.universe.life.trade.privacy.enums.TradeOrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交易订单DTO
 *
 * @author universe-life
 */
@Data
public class TradeOrderDTO {

    private Long id;

    private Long taskId;

    private String taskTitle;

    private Long publisherId;

    private String publisherName;

    private Long acceptorId;

    private String acceptorName;

    private Long rewardAmount;

    private TradeOrderStatus status;

    private String submitContent;

    private String submitImagesJson;

    private String rejectReason;

    private LocalDateTime appliedAt;

    private LocalDateTime approvedAt;

    private LocalDateTime submittedAt;

    private LocalDateTime completedAt;

    private LocalDateTime createdAt;

    private LocalDateTime deadline;
}
