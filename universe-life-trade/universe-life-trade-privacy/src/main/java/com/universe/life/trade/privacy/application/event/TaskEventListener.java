package com.universe.life.trade.privacy.application.event;

import com.universe.life.common.util.CacheUtil;
import com.universe.life.trade.privacy.domain.event.TaskCancelledEvent;
import com.universe.life.trade.privacy.domain.event.TaskStatusChangedEvent;
import com.universe.life.trade.privacy.domain.model.aggregate.TradeOrderAggregate;
import com.universe.life.trade.privacy.domain.repository.TradeOrderRepository;
import com.universe.life.trade.privacy.infrastructure.constants.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 任务事件监听器
 * <p>
 * 监听来自任务服务的任务事件，同步任务状态快照或处理任务取消。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventListener {

    private final TradeOrderRepository orderRepository;
    private final CacheUtil cacheUtil;

    /**
     * 处理任务状态变更事件
     * <p>
     * 同步任务状态快照到订单中，保持数据一致性。
     * </p>
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(
                            name = "trade.task.status.changed.queue",
                            durable = "true",
                            autoDelete = "false"
                    ),
                    exchange = @Exchange(
                            name = "task.exchange",
                            type = ExchangeTypes.TOPIC
                    ),
                    key = "task.status.changed"
            )
    )
    @Transactional(rollbackFor = Exception.class)
    public void handleTaskStatusChanged(TaskStatusChangedEvent event) {
        log.info("收到任务状态变更事件: taskId={}, oldStatus={}, newStatus={}", 
                event.getTaskId(), event.getOldStatus(), event.getNewStatus());

        try {
            // 查询该任务的所有订单
            List<TradeOrderAggregate> orders = orderRepository.findAllByTaskId(event.getTaskId());
            
            // 更新订单中的任务状态快照
            for (TradeOrderAggregate order : orders) {
                // TODO: 更新订单中的任务状态快照字段
                // order.updateTaskStatusSnapshot(event.getNewStatus());
                // orderRepository.save(order);
                
                // 删除订单详情缓存
                invalidateOrderCache(order.getIdValue());
            }

            log.info("同步任务状态到订单: taskId={}, orderCount={}", 
                    event.getTaskId(), orders.size());

        } catch (Exception e) {
            log.error("处理任务状态变更事件失败: taskId={}", event.getTaskId(), e);
            throw e;
        }
    }

    /**
     * 处理任务取消事件
     * <p>
     * 当任务被取消时，取消所有相关的订单。
     * </p>
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(
                            name = "trade.task.cancelled.queue",
                            durable = "true",
                            autoDelete = "false"
                    ),
                    exchange = @Exchange(
                            name = "task.exchange",
                            type = ExchangeTypes.TOPIC
                    ),
                    key = "task.cancelled"
            )
    )
    @Transactional(rollbackFor = Exception.class)
    public void handleTaskCancelled(TaskCancelledEvent event) {
        log.info("收到任务取消事件: taskId={}, reason={}", 
                event.getTaskId(), event.getCancelReason());

        try {
            // 查询该任务的所有订单
            List<TradeOrderAggregate> orders = orderRepository.findAllByTaskId(event.getTaskId());
            
            // 取消所有相关订单
            int cancelledCount = 0;
            for (TradeOrderAggregate order : orders) {
                // 只取消未完成的订单
                if (!order.getStatus().isCompleted() && !order.getStatus().isCancelled()) {
                    // 取消订单
                    order.cancel(event.getCancelReason());
                    orderRepository.save(order);
                    cancelledCount++;

                    // 删除订单详情缓存
                    invalidateOrderCache(order.getIdValue());
                }
            }

            log.info("取消任务相关订单: taskId={}, totalOrders={}, cancelledCount={}",
                    event.getTaskId(), orders.size(), cancelledCount);

        } catch (Exception e) {
            log.error("处理任务取消事件失败: taskId={}", event.getTaskId(), e);
            throw e;
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 删除订单详情缓存
     */
    private void invalidateOrderCache(Long orderId) {
        String cacheKey = RedisKeyConstants.ORDER_DETAIL + orderId;
        cacheUtil.delete(cacheKey);
        log.debug("删除订单详情缓存: orderId={}", orderId);
    }
}
