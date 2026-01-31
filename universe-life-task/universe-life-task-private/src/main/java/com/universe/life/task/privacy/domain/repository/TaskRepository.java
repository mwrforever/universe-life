package com.universe.life.task.privacy.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.task.privacy.domain.model.Task;

import java.util.List;
import java.util.Optional;

/**
 * Task 仓储接口
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
public interface TaskRepository {

    /**
     * 保存任务
     */
    Task save(Task task);

    /**
     * 根据ID查询任务
     */
    Optional<Task> findById(Long taskId);

    /**
     * 根据发布者ID分页查询任务列表
     */
    Page<Task> findByPublisherId(Long publisherId, TaskStatus status, Page<Task> page);

    /**
     * 分页查询任务大厅列表
     */
    Page<Task> findHallTasks(Long categoryId, Long minReward, Long maxReward, 
                              String sortBy, String sortOrder, Page<Task> page);

    /**
     * 更新任务状态（乐观锁）
     */
    boolean updateStatus(Long taskId, TaskStatus oldStatus, TaskStatus newStatus, Integer version);

    /**
     * 增加接单人数
     */
    boolean incrementAcceptorCount(Long taskId, Integer version);

    /**
     * 减少接单人数
     */
    boolean decrementAcceptorCount(Long taskId, Integer version);

    /**
     * 批量查询任务
     */
    List<Task> findByIds(List<Long> taskIds);

    /**
     * 根据状态分页查询任务列表
     */
    Page<Task> findByStatus(TaskStatus status, Page<Task> page);
}
