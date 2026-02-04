package com.universe.life.task.privacy.application.event;

import com.universe.life.common.util.CacheUtil;
import com.universe.life.task.privacy.domain.event.OrderApprovedEvent;
import com.universe.life.task.privacy.domain.event.OrderCompletedEvent;
import com.universe.life.task.privacy.domain.event.OrderCreatedEvent;
import com.universe.life.task.privacy.domain.event.OrderRejectedEvent;
import com.universe.life.task.privacy.domain.exception.TaskNotFoundException;
import com.universe.life.task.privacy.domain.model.Task;
import com.universe.life.task.privacy.domain.repository.TaskRepository;
import com.universe.life.task.privacy.infrastructure.enums.TaskStatus;
import com.universe.life.task.privacy.infrastructure.constants.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单事件监听器
 * 监听来自交易服务的订单事件，更新任务状态
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final TaskRepository taskRepository;
    private final CacheUtil cacheUtil;
    private final TaskEventPublisher taskEventPublisher;

    /**
     * 处理订单创建事件
     * 增加任务的当前接单人数
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(
                            name = "task.order.created.queue",
                            durable = "true",
                            autoDelete = "false"
                    ),
                    exchange = @Exchange(
                            name = "trade.exchange",
                            type = ExchangeTypes.TOPIC
                    ),
                    key = "order.created"
            )
    )
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("收到订单创建事件: orderId={}, taskId={}, acceptorId={}", 
                event.getOrderId(), event.getTaskId(), event.getAcceptorId());

        try {
            // 查询任务
            Task task = taskRepository.findById(event.getTaskId())
                    .orElseThrow(() -> new TaskNotFoundException(event.getTaskId()));

            // 增加接单人数
            task.incrementAcceptors();
            taskRepository.save(task);
            
            // 删除任务详情缓存
            invalidateTaskCache(event.getTaskId());

            log.info("任务接单人数增加: taskId={}, currentAcceptors={}", 
                    task.getTaskId(), task.getCurrentAcceptors());

        } catch (Exception e) {
            log.error("处理订单创建事件失败: orderId={}, taskId={}", 
                    event.getOrderId(), event.getTaskId(), e);
            throw e;
        }
    }

    /**
     * 处理订单审批通过事件
     * 当所有订单都审批通过后，更新任务状态为 WAIT_PAY
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(
                            name = "task.order.approved.queue",
                            durable = "true",
                            autoDelete = "false"
                    ),
                    exchange = @Exchange(
                            name = "trade.exchange",
                            type = ExchangeTypes.TOPIC
                    ),
                    key = "order.approved"
            )
    )
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderApproved(OrderApprovedEvent event) {
        log.info("收到订单审批通过事件: orderId={}, taskId={}", 
                event.getOrderId(), event.getTaskId());

        try {
            // 查询任务
            Task task = taskRepository.findById(event.getTaskId())
                    .orElseThrow(() -> new TaskNotFoundException(event.getTaskId()));

            // 检查是否所有订单都已审批通过
            // TODO: 需要查询订单服务获取该任务的所有订单状态
            // 如果所有订单都审批通过，则更新任务状态为 WAIT_PAY
            if (task.getStatus().isRecruiting() && 
                task.getCurrentAcceptors() >= task.getMaxAcceptors()) {
                
                task.startPayment();
                taskRepository.save(task);
                
                // 删除任务详情缓存和大厅列表缓存
                invalidateTaskCache(event.getTaskId());
                invalidateHallCache();
                
                // 发布任务状态变更事件
                taskEventPublisher.publishTaskStatusChanged(task);
                
                log.info("任务状态更新为待支付: taskId={}", task.getTaskId());
            }

        } catch (Exception e) {
            log.error("处理订单审批通过事件失败: orderId={}, taskId={}", 
                    event.getOrderId(), event.getTaskId(), e);
            throw e;
        }
    }

    /**
     * 处理订单审批拒绝事件
     * 减少任务的当前接单人数
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(
                            name = "task.order.rejected.queue",
                            durable = "true",
                            autoDelete = "false"
                    ),
                    exchange = @Exchange(
                            name = "trade.exchange",
                            type = ExchangeTypes.TOPIC
                    ),
                    key = "order.rejected"
            )
    )
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderRejected(OrderRejectedEvent event) {
        log.info("收到订单审批拒绝事件: orderId={}, taskId={}, reason={}", 
                event.getOrderId(), event.getTaskId(), event.getRejectReason());

        try {
            // 查询任务
            Task task = taskRepository.findById(event.getTaskId())
                    .orElseThrow(() -> new TaskNotFoundException(event.getTaskId()));

            // 减少接单人数
            task.decrementAcceptors();
            taskRepository.save(task);
            
            // 删除任务详情缓存
            invalidateTaskCache(event.getTaskId());

            log.info("任务接单人数减少: taskId={}, currentAcceptors={}", 
                    task.getTaskId(), task.getCurrentAcceptors());

        } catch (Exception e) {
            log.error("处理订单审批拒绝事件失败: orderId={}, taskId={}", 
                    event.getOrderId(), event.getTaskId(), e);
            throw e;
        }
    }

    /**
     * 处理订单完成事件
     * 检查是否所有订单都已完成，如果是则更新任务状态为 COMPLETED
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(
                            name = "task.order.completed.queue",
                            durable = "true",
                            autoDelete = "false"
                    ),
                    exchange = @Exchange(
                            name = "trade.exchange",
                            type = ExchangeTypes.TOPIC
                    ),
                    key = "order.completed"
            )
    )
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderCompleted(OrderCompletedEvent event) {
        log.info("收到订单完成事件: orderId={}, taskId={}", 
                event.getOrderId(), event.getTaskId());

        try {
            // 查询任务
            Task task = taskRepository.findById(event.getTaskId())
                    .orElseThrow(() -> new TaskNotFoundException(event.getTaskId()));

            // TODO: 需要查询订单服务获取该任务的所有订单状态
            // 检查是否所有订单都已完成
            // 如果所有订单都完成，则更新任务状态为 COMPLETED
            if (task.getStatus() == TaskStatus.PROGRESS) {
                // 这里简化处理，实际应该查询所有订单状态
                task.complete();
                taskRepository.save(task);
                
                // 删除任务详情缓存和大厅列表缓存
                invalidateTaskCache(event.getTaskId());
                invalidateHallCache();
                
                // 发布任务完成事件
                taskEventPublisher.publishTaskCompleted(task);
                
                log.info("任务状态更新为已完成: taskId={}", task.getTaskId());
            }

        } catch (Exception e) {
            log.error("处理订单完成事件失败: orderId={}, taskId={}", 
                    event.getOrderId(), event.getTaskId(), e);
            throw e;
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 删除任务详情缓存
     */
    private void invalidateTaskCache(Long taskId) {
        String cacheKey = RedisKeyConstants.TASK_DETAIL + taskId;
        cacheUtil.delete(cacheKey);
        log.debug("删除任务详情缓存: taskId={}", taskId);
    }

    /**
     * 删除任务大厅列表缓存（删除整个Hash）
     */
    private void invalidateHallCache() {
        cacheUtil.hDeleteAll(RedisKeyConstants.TASK_HALL);
        log.debug("删除任务大厅列表缓存: key={}", RedisKeyConstants.TASK_HALL);
    }
}
