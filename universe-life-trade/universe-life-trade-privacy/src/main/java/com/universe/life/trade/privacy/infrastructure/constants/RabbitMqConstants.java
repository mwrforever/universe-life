package com.universe.life.trade.privacy.infrastructure.constants;

/**
 * RabbitMQ 常量定义
 * 定义交易服务使用的Exchange、Queue和RoutingKey
 *
 * @author Universe Life Team
 * @since 2025-01-18
 */
public class RabbitMqConstants {

    /**
     * Exchange 常量
     */
    public static class Exchange {
        /**
         * 交易服务Exchange
         */
        public static final String TRADE_EXCHANGE = "trade.exchange";
        
        /**
         * 任务服务Exchange
         */
        public static final String TASK_EXCHANGE = "task.exchange";
    }

    /**
     * Queue 常量
     */
    public static class Queue {
        // ==================== 交易服务发布的队列 ====================
        
        /**
         * 订单创建事件队列
         */
        public static final String ORDER_CREATED_QUEUE = "order.created.queue";
        
        /**
         * 订单审批通过事件队列
         */
        public static final String ORDER_APPROVED_QUEUE = "order.approved.queue";
        
        /**
         * 订单审批拒绝事件队列
         */
        public static final String ORDER_REJECTED_QUEUE = "order.rejected.queue";
        
        /**
         * 订单状态变更事件队列
         */
        public static final String ORDER_STATUS_CHANGED_QUEUE = "order.status.changed.queue";
        
        /**
         * 订单完成事件队列
         */
        public static final String ORDER_COMPLETED_QUEUE = "order.completed.queue";
        
        /**
         * 申诉创建事件队列
         */
        public static final String APPEAL_CREATED_QUEUE = "appeal.created.queue";
        
        // ==================== 交易服务订阅的队列 ====================
        
        /**
         * 任务状态变更事件队列（交易服务订阅）
         */
        public static final String TASK_STATUS_CHANGED_QUEUE = "task.status.changed.trade.queue";
        
        /**
         * 任务取消事件队列（交易服务订阅）
         */
        public static final String TASK_CANCELLED_QUEUE = "task.cancelled.trade.queue";
    }

    /**
     * RoutingKey 常量
     */
    public static class RoutingKey {
        // ==================== 交易服务发布的路由键 ====================
        
        /**
         * 订单创建事件路由键
         */
        public static final String ORDER_CREATED = "order.created";
        
        /**
         * 订单审批通过事件路由键
         */
        public static final String ORDER_APPROVED = "order.approved";
        
        /**
         * 订单审批拒绝事件路由键
         */
        public static final String ORDER_REJECTED = "order.rejected";
        
        /**
         * 订单状态变更事件路由键
         */
        public static final String ORDER_STATUS_CHANGED = "order.status.changed";
        
        /**
         * 订单完成事件路由键
         */
        public static final String ORDER_COMPLETED = "order.completed";
        
        /**
         * 申诉创建事件路由键
         */
        public static final String APPEAL_CREATED = "appeal.created";
        
        // ==================== 交易服务订阅的路由键 ====================
        
        /**
         * 任务状态变更事件路由键
         */
        public static final String TASK_STATUS_CHANGED = "task.status.changed";
        
        /**
         * 任务取消事件路由键
         */
        public static final String TASK_CANCELLED = "task.cancelled";
    }
}
