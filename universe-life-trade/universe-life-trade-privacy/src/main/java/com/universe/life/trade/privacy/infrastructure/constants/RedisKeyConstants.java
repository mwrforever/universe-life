package com.universe.life.trade.privacy.infrastructure.constants;

/**
 * Redis 缓存键常量定义
 * 定义交易服务使用的所有缓存键前缀
 *
 * @author Universe Life Team
 * @since 2025-01-18
 */
public class RedisKeyConstants {

    /**
     * 缓存键前缀：订单详情
     * 格式：order:detail:{orderId}
     */
    public static final String ORDER_DETAIL = "order:detail:";
    
    /**
     * 缓存键前缀：我的接单列表
     * 格式：order:my:{acceptorId}:{status}:{pageNum}
     */
    public static final String ORDER_MY_LIST = "order:my:";
    
    /**
     * 缓存键前缀：任务的接单列表
     * 格式：order:task:{taskId}:{status}:{pageNum}
     */
    public static final String ORDER_TASK_LIST = "order:task:";
    
    /**
     * 缓存键前缀：申诉列表
     * 格式：appeal:list:{status}:{pageNum}
     */
    public static final String APPEAL_LIST = "appeal:list:";
    
    /**
     * 缓存键前缀：申诉详情
     * 格式：appeal:detail:{appealId}
     */
    public static final String APPEAL_DETAIL = "appeal:detail:";
    
    /**
     * 构建订单详情缓存键
     *
     * @param orderId 订单ID
     * @return 缓存键
     */
    public static String buildOrderDetailKey(Long orderId) {
        return ORDER_DETAIL + orderId;
    }
    
    /**
     * 构建我的接单列表缓存键
     *
     * @param acceptorId 接单者ID
     * @param status 订单状态（可为null）
     * @param pageNum 页码
     * @return 缓存键
     */
    public static String buildOrderMyListKey(Long acceptorId, Integer status, Integer pageNum) {
        return ORDER_MY_LIST + acceptorId + ":" + status + ":" + pageNum;
    }
    
    /**
     * 构建任务的接单列表缓存键
     *
     * @param taskId 任务ID
     * @param status 订单状态（可为null）
     * @param pageNum 页码
     * @return 缓存键
     */
    public static String buildOrderTaskListKey(Long taskId, Integer status, Integer pageNum) {
        return ORDER_TASK_LIST + taskId + ":" + status + ":" + pageNum;
    }
    
    /**
     * 构建申诉列表缓存键
     *
     * @param status 申诉状态（可为null）
     * @param pageNum 页码
     * @return 缓存键
     */
    public static String buildAppealListKey(Integer status, Integer pageNum) {
        return APPEAL_LIST + status + ":" + pageNum;
    }
    
    /**
     * 构建申诉详情缓存键
     *
     * @param appealId 申诉ID
     * @return 缓存键
     */
    public static String buildAppealDetailKey(Long appealId) {
        return APPEAL_DETAIL + appealId;
    }
}
