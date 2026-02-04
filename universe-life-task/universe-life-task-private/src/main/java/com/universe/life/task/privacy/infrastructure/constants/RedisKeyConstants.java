package com.universe.life.task.privacy.infrastructure.constants;

/**
 * Redis 缓存键常量
 * 定义所有缓存键的前缀
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
public class RedisKeyConstants {

    /**
     * 任务详情缓存键前缀
     * 格式: task:detail:{taskId}
     */
    public static final String TASK_DETAIL = "task:detail:";

    /**
     * 任务大厅列表缓存键（使用Hash结构）
     * 格式: task:hall
     * Hash的field格式: {categoryId}:{minReward}:{maxReward}:{sortBy}:{sortOrder}:{pageNum}
     */
    public static final String TASK_HALL = "task:hall";

    /**
     * 任务分类列表缓存键（使用Hash结构）
     * 格式: task:categories
     */
    public static final String TASK_CATEGORIES_HASH = "task:categories";

    /**
     * 任务分类列表缓存键
     * 格式: task:categories
     */
    public static final String TASK_CATEGORIES = "task:categories";

    private RedisKeyConstants() {
        // 工具类，禁止实例化
    }
}
