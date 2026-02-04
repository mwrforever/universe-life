package com.universe.life.task.privacy.domain.model;

import com.universe.life.task.privacy.infrastructure.enums.TaskDepositStatus;
import com.universe.life.task.privacy.infrastructure.enums.TaskReviewStatus;
import com.universe.life.task.privacy.infrastructure.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Task 聚合根 - 任务领域模型
 * 封装任务的业务规则和状态转换逻辑
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

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

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 乐观锁版本号 */
    private Long version;

    // ==================== 业务方法 ====================

    /**
     * 创建任务
     * 初始化状态为待审核，计算保证金为悬赏金额的50%
     */
    public static Task create(String title, String description, Long rewardAmount,
                              Long categoryId, LocalDateTime deadline, Integer maxAcceptors,
                              Long publisherId) {
        // 计算保证金（悬赏金额的50%）
        Long depositAmount = rewardAmount / 2;

        return Task.builder()
                .title(title)
                .description(description)
                .rewardAmount(rewardAmount)
                .depositAmount(depositAmount)
                .depositStatus(TaskDepositStatus.UNPAID)
                .categoryId(categoryId)
                .deadline(deadline)
                .maxAcceptors(maxAcceptors)
                .currentAcceptors(0)
                .status(TaskStatus.PENDING)
                .reviewStatus(TaskReviewStatus.PENDING)
                .publisherId(publisherId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(0L)
                .build();
    }

    /**
     * 审核通过
     * 状态从 PENDING 变更为 RECRUITING
     */
    public void approve() {
        validateStatusTransition(TaskStatus.RECRUITING);
        this.status = TaskStatus.RECRUITING;
        this.reviewStatus = TaskReviewStatus.APPROVED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 审核拒绝
     * 状态从 PENDING 变更为 REJECTED
     */
    public void reject() {
        validateStatusTransition(TaskStatus.REJECTED);
        this.status = TaskStatus.REJECTED;
        this.reviewStatus = TaskReviewStatus.REJECTED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 开始支付保证金
     * 状态从 WAIT_PAY 变更为 PAYING
     */
    public void startPayment() {
        validateStatusTransition(TaskStatus.PAYING);
        this.status = TaskStatus.PAYING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 完成支付
     * 状态从 PAYING 变更为 PROGRESS，保证金状态变更为已支付
     */
    public void completePayment() {
        validateStatusTransition(TaskStatus.PROGRESS);
        this.status = TaskStatus.PROGRESS;
        this.depositStatus = TaskDepositStatus.PAID;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 支付失败
     * 状态从 PAYING 变更为 PAY_FAIL
     */
    public void failPayment() {
        validateStatusTransition(TaskStatus.PAY_FAIL);
        this.status = TaskStatus.PAY_FAIL;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 取消任务
     * 可以从 RECRUITING、WAIT_PAY、PAYING 状态取消
     */
    public void cancel() {
        validateStatusTransition(TaskStatus.CANCELLED);
        this.status = TaskStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 完成任务
     * 状态从 PROGRESS 变更为 COMPLETED
     */
    public void complete() {
        validateStatusTransition(TaskStatus.COMPLETED);
        this.status = TaskStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新为待支付状态
     * 状态从 RECRUITING 变更为 WAIT_PAY
     */
    public void updateToWaitPay() {
        validateStatusTransition(TaskStatus.WAIT_PAY);
        this.status = TaskStatus.WAIT_PAY;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 下架任务
     * 状态从 RECRUITING 变更为 OFFLINE
     */
    public void offline() {
        validateStatusTransition(TaskStatus.OFFLINE);
        this.status = TaskStatus.OFFLINE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 增加接单人数
     */
    public void incrementAcceptorCount() {
        if (this.currentAcceptors >= this.maxAcceptors) {
            throw new IllegalStateException("接单人数已达上限");
        }
        this.currentAcceptors++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 减少接单人数
     */
    public void decrementAcceptorCount() {
        if (this.currentAcceptors <= 0) {
            throw new IllegalStateException("接单人数不能为负数");
        }
        this.currentAcceptors--;
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== 业务规则验证 ====================

    /**
     * 判断是否可以编辑
     * 只有待审核或审核拒绝状态可以编辑
     */
    public boolean canEdit() {
        return this.status == TaskStatus.PENDING || this.status == TaskStatus.REJECTED;
    }

    /**
     * 判断是否可以取消
     * 招募中、待支付、支付中状态可以取消
     */
    public boolean canCancel() {
        return this.status == TaskStatus.RECRUITING
                || this.status == TaskStatus.WAIT_PAY
                || this.status == TaskStatus.PAYING;
    }

    /**
     * 判断是否可以直接取消（无人接单）
     */
    public boolean canDirectCancel() {
        return this.status.isRecruiting() && this.currentAcceptors == 0;
    }

    /**
     * 判断是否需要退款流程
     */
    public boolean needRefund() {
        return (this.status.isWaitPay() || this.status.isPaying()) 
                && this.depositStatus.isPaid();
    }

    /**
     * 判断任务是否已过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.deadline);
    }

    /**
     * 判断是否达到接单人数上限
     */
    public boolean isAcceptorLimitReached() {
        return this.currentAcceptors >= this.maxAcceptors;
    }

    /**
     * 验证状态转换是否合法
     */
    private void validateStatusTransition(TaskStatus targetStatus) {
        if (!isValidTransition(this.status, targetStatus)) {
            throw new IllegalStateException(
                String.format("非法的状态转换: %s -> %s", 
                    this.status.getText(), targetStatus.getText())
            );
        }
    }

    /**
     * 判断状态转换是否合法
     */
    private boolean isValidTransition(TaskStatus from, TaskStatus to) {
        if (from == null || to == null) {
            return false;
        }

        return switch (from) {
            case PENDING -> to == TaskStatus.RECRUITING || to == TaskStatus.REJECTED;
            case RECRUITING -> to == TaskStatus.WAIT_PAY || to == TaskStatus.CANCELLED 
                            || to == TaskStatus.OFFLINE;
            case WAIT_PAY -> to == TaskStatus.PAYING || to == TaskStatus.CANCELLED;
            case PAYING -> to == TaskStatus.PROGRESS || to == TaskStatus.PAY_FAIL 
                        || to == TaskStatus.CANCELLED;
            case PAY_FAIL -> to == TaskStatus.PAYING || to == TaskStatus.CANCELLED;
            case PROGRESS -> to == TaskStatus.COMPLETED;
            default -> false;
        };
    }

    /**
     * 验证任务是否可以被指定用户编辑
     */
    public boolean canBeEditedBy(Long userId) {
        return this.publisherId.equals(userId) && canEdit();
    }

    /**
     * 验证任务是否可以被指定用户取消
     */
    public boolean canBeCancelledBy(Long userId) {
        return this.publisherId.equals(userId) && canCancel();
    }

    // ==================== 接单人数管理 ====================

    /**
     * 增加接单人数
     */
    public void incrementAcceptors() {
        if (this.currentAcceptors == null) {
            this.currentAcceptors = 0;
        }
        if (this.currentAcceptors >= this.maxAcceptors) {
            throw new IllegalStateException("接单人数已达上限");
        }
        this.currentAcceptors++;
    }

    /**
     * 减少接单人数
     */
    public void decrementAcceptors() {
        if (this.currentAcceptors == null || this.currentAcceptors <= 0) {
            throw new IllegalStateException("接单人数不能为负数");
        }
        this.currentAcceptors--;
    }
}
