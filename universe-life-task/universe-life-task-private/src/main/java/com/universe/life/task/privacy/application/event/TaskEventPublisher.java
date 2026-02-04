package com.universe.life.task.privacy.application.event;

import com.universe.life.common.util.RabbitMqSender;
import com.universe.life.task.privacy.domain.event.*;
import com.universe.life.task.privacy.domain.model.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 任务事件发布器
 * 负责发布任务相关的领域事件到 RabbitMQ
 *
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventPublisher {

    private static final String TASK_EXCHANGE = "task.exchange";

    private final RabbitMqSender rabbitMqSender;

    /**
     * 发布任务创建事件
     */
    public void publishTaskCreated(Task task) {
        TaskCreatedEvent event = TaskCreatedEvent.builder()
                .taskId(task.getTaskId())
                .title(task.getTitle())
                .publisherId(task.getPublisherId())
                .rewardAmount(task.getRewardAmount())
                .categoryId(task.getCategoryId())
                .createdAt(task.getCreatedAt())
                .build();

        rabbitMqSender.builder()
                .to(TASK_EXCHANGE, "task.created")
                .persistent(true)
                .send(event);
        log.info("发布任务创建事件: taskId={}, title={}", task.getTaskId(), task.getTitle());
    }

    /**
     * 发布任务审核通过事件
     */
    public void publishTaskApproved(Task task, Long reviewerId) {
        TaskApprovedEvent event = TaskApprovedEvent.builder()
                .taskId(task.getTaskId())
                .title(task.getTitle())
                .publisherId(task.getPublisherId())
                .reviewerId(reviewerId)
                .approvedAt(LocalDateTime.now())
                .build();

        rabbitMqSender.builder()
                .to(TASK_EXCHANGE, "task.approved")
                .persistent(true)
                .send(event);
        log.info("发布任务审核通过事件: taskId={}, reviewerId={}", task.getTaskId(), reviewerId);
    }

    /**
     * 发布任务审核拒绝事件
     */
    public void publishTaskRejected(Task task, Long reviewerId, String rejectReason) {
        TaskRejectedEvent event = TaskRejectedEvent.builder()
                .taskId(task.getTaskId())
                .title(task.getTitle())
                .publisherId(task.getPublisherId())
                .reviewerId(reviewerId)
                .rejectReason(rejectReason)
                .rejectedAt(LocalDateTime.now())
                .build();

        rabbitMqSender.builder()
                .to(TASK_EXCHANGE, "task.rejected")
                .persistent(true)
                .send(event);
        log.info("发布任务审核拒绝事件: taskId={}, reviewerId={}, reason={}",
                task.getTaskId(), reviewerId, rejectReason);
    }

    /**
     * 发布任务状态变更事件
     */
    public void publishTaskStatusChanged(Task task) {
        TaskStatusChangedEvent event = TaskStatusChangedEvent.builder()
                .taskId(task.getTaskId())
                .title(task.getTitle())
                .oldStatus(null) // TODO: 需要在Task中记录旧状态
                .newStatus(task.getStatus())
                .changedAt(LocalDateTime.now())
                .build();

        rabbitMqSender.builder()
                .to(TASK_EXCHANGE, "task.status.changed")
                .persistent(true)
                .send(event);
        log.info("发布任务状态变更事件: taskId={}, status={}", task.getTaskId(), task.getStatus());
    }

    /**
     * 发布任务取消事件
     */
    public void publishTaskCancelled(Task task, String cancelReason) {
        TaskCancelledEvent event = TaskCancelledEvent.builder()
                .taskId(task.getTaskId())
                .title(task.getTitle())
                .publisherId(task.getPublisherId())
                .cancelReason(cancelReason)
                .cancelledAt(LocalDateTime.now())
                .build();

        rabbitMqSender.builder()
                .to(TASK_EXCHANGE, "task.cancelled")
                .persistent(true)
                .send(event);
        log.info("发布任务取消事件: taskId={}, reason={}", task.getTaskId(), cancelReason);
    }

    /**
     * 发布任务完成事件
     */
    public void publishTaskCompleted(Task task) {
        TaskCompletedEvent event = TaskCompletedEvent.builder()
                .taskId(task.getTaskId())
                .title(task.getTitle())
                .publisherId(task.getPublisherId())
                .completedAt(LocalDateTime.now())
                .totalAcceptors(task.getCurrentAcceptors())
                .totalRewardAmount(task.getRewardAmount() * task.getCurrentAcceptors())
                .build();

        rabbitMqSender.builder()
                .to(TASK_EXCHANGE, "task.completed")
                .persistent(true)
                .send(event);
        log.info("发布任务完成事件: taskId={}, totalAcceptors={}",
                task.getTaskId(), task.getCurrentAcceptors());
    }
}
