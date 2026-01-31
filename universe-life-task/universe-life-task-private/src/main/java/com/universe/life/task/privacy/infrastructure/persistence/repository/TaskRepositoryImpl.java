package com.universe.life.task.privacy.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.task.privacy.domain.model.Task;
import com.universe.life.task.privacy.domain.repository.TaskRepository;
import com.universe.life.task.privacy.infrastructure.persistence.mapper.TaskMapper;
import com.universe.life.task.privacy.infrastructure.persistence.po.TaskPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Task 仓储实现
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Repository
@RequiredArgsConstructor
public class TaskRepositoryImpl implements TaskRepository {

    private final TaskMapper taskMapper;

    @Override
    public Task save(Task task) {
        TaskPO po = toTaskPO(task);
        if (po.getTaskId() == null) {
            taskMapper.insert(po);
        } else {
            taskMapper.updateById(po);
        }
        return toTask(po);
    }

    @Override
    public Optional<Task> findById(Long taskId) {
        TaskPO po = taskMapper.selectById(taskId);
        return Optional.ofNullable(po).map(this::toTask);
    }

    @Override
    public Page<Task> findByPublisherId(Long publisherId, TaskStatus status, Page<Task> page) {
        LambdaQueryWrapper<TaskPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskPO::getPublisherId, publisherId);
        if (status != null) {
            wrapper.eq(TaskPO::getStatus, status);
        }
        wrapper.orderByDesc(TaskPO::getCreatedAt);

        Page<TaskPO> poPage = new Page<>(page.getCurrent(), page.getSize());
        taskMapper.selectPage(poPage, wrapper);

        Page<Task> result = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        result.setRecords(poPage.getRecords().stream()
                .map(this::toTask)
                .collect(Collectors.toList()));
        return result;
    }

    @Override
    public Page<Task> findHallTasks(Long categoryId, Long minReward, Long maxReward,
                                     String sortBy, String sortOrder, Page<Task> page) {
        LambdaQueryWrapper<TaskPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskPO::getStatus, TaskStatus.RECRUITING);
        
        if (categoryId != null) {
            wrapper.eq(TaskPO::getCategoryId, categoryId);
        }
        if (minReward != null) {
            wrapper.ge(TaskPO::getRewardAmount, minReward);
        }
        if (maxReward != null) {
            wrapper.le(TaskPO::getRewardAmount, maxReward);
        }

        // 排序
        if ("rewardAmount".equals(sortBy)) {
            wrapper.orderBy(true, "asc".equalsIgnoreCase(sortOrder), TaskPO::getRewardAmount);
        } else if ("deadline".equals(sortBy)) {
            wrapper.orderBy(true, "asc".equalsIgnoreCase(sortOrder), TaskPO::getDeadline);
        } else {
            wrapper.orderByDesc(TaskPO::getCreatedAt);
        }

        Page<TaskPO> poPage = new Page<>(page.getCurrent(), page.getSize());
        taskMapper.selectPage(poPage, wrapper);

        Page<Task> result = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        result.setRecords(poPage.getRecords().stream()
                .map(this::toTask)
                .collect(Collectors.toList()));
        return result;
    }

    @Override
    public boolean updateStatus(Long taskId, TaskStatus oldStatus, TaskStatus newStatus, Integer version) {
        LambdaUpdateWrapper<TaskPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TaskPO::getTaskId, taskId)
                .eq(TaskPO::getStatus, oldStatus)
                .eq(TaskPO::getVersion, version)
                .set(TaskPO::getStatus, newStatus);
        return taskMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean incrementAcceptorCount(Long taskId, Integer version) {
        LambdaUpdateWrapper<TaskPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TaskPO::getTaskId, taskId)
                .eq(TaskPO::getVersion, version)
                .setSql("current_acceptors = current_acceptors + 1");
        return taskMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean decrementAcceptorCount(Long taskId, Integer version) {
        LambdaUpdateWrapper<TaskPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TaskPO::getTaskId, taskId)
                .eq(TaskPO::getVersion, version)
                .setSql("current_acceptors = current_acceptors - 1");
        return taskMapper.update(null, wrapper) > 0;
    }

    @Override
    public List<Task> findByIds(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return List.of();
        }
        List<TaskPO> poList = taskMapper.selectBatchIds(taskIds);
        return poList.stream().map(this::toTask).collect(Collectors.toList());
    }

    @Override
    public Page<Task> findByStatus(TaskStatus status, Page<Task> page) {
        LambdaQueryWrapper<TaskPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskPO::getStatus, status)
                .orderByDesc(TaskPO::getCreatedAt);

        Page<TaskPO> poPage = new Page<>(page.getCurrent(), page.getSize());
        taskMapper.selectPage(poPage, wrapper);

        Page<Task> result = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        result.setRecords(poPage.getRecords().stream()
                .map(this::toTask)
                .collect(Collectors.toList()));
        return result;
    }

    // ==================== 对象转换 ====================

    private Task toTask(TaskPO po) {
        return Task.builder()
                .taskId(po.getTaskId())
                .title(po.getTitle())
                .description(po.getDescription())
                .rewardAmount(po.getRewardAmount())
                .depositAmount(po.getDepositAmount())
                .depositStatus(po.getDepositStatus())
                .categoryId(po.getCategoryId())
                .deadline(po.getDeadline())
                .maxAcceptors(po.getMaxAcceptors())
                .currentAcceptors(po.getCurrentAcceptors())
                .status(po.getStatus())
                .reviewStatus(po.getReviewStatus())
                .publisherId(po.getPublisherId())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .version(po.getVersion())
                .build();
    }

    private TaskPO toTaskPO(Task task) {
        return TaskPO.builder()
                .taskId(task.getTaskId())
                .title(task.getTitle())
                .description(task.getDescription())
                .rewardAmount(task.getRewardAmount())
                .depositAmount(task.getDepositAmount())
                .depositStatus(task.getDepositStatus())
                .categoryId(task.getCategoryId())
                .deadline(task.getDeadline())
                .maxAcceptors(task.getMaxAcceptors())
                .currentAcceptors(task.getCurrentAcceptors())
                .status(task.getStatus())
                .reviewStatus(task.getReviewStatus())
                .publisherId(task.getPublisherId())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .version(task.getVersion())
                .build();
    }
}
