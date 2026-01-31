package com.universe.life.task.privacy.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Task DTO
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

    /** 任务ID */
    private Long taskId;

    /** 任务标题 */
    private String title;

    /** 任务描述 */
    private String description;

    /** 悬赏金额（分） */
    private Long rewardAmount;

    /** 保证金金额（分） */
    private Long depositAmount;

    /** 保证金状态 */
    private TaskDepositStatus depositStatus;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称 */
    private String categoryName;

    /** 截止时间 */
    private LocalDateTime deadline;

    /** 最大接单人数 */
    private Integer maxAcceptors;

    /** 当前接单人数 */
    private Integer currentAcceptors;

    /** 任务状态 */
    private TaskStatus status;

    /** 审核状态 */
    private TaskReviewStatus reviewStatus;

    /** 发布者ID */
    private Long publisherId;

    /** 发布者昵称 */
    private String publisherNickname;

    /** 发布者头像 */
    private String publisherAvatar;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 乐观锁版本号 */
    private Integer version;
}
