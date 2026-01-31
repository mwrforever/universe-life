package com.universe.life.task.privacy.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.common.util.CacheUtil;
import com.universe.life.task.privacy.application.assembler.TaskAssembler;
import com.universe.life.task.privacy.application.dto.TaskDTO;
import com.universe.life.task.privacy.domain.exception.TaskAccessDeniedException;
import com.universe.life.task.privacy.domain.exception.TaskInvalidStatusException;
import com.universe.life.task.privacy.domain.exception.TaskNotFoundException;
import com.universe.life.task.privacy.domain.model.Task;
import com.universe.life.task.privacy.domain.repository.TaskRepository;
import com.universe.life.task.privacy.infrastructure.constants.RedisKeyConstants;
import com.universe.life.task.privacy.interfaces.dto.request.TaskCreateRequest;
import com.universe.life.task.privacy.interfaces.dto.request.TaskHallQueryRequest;
import com.universe.life.task.privacy.interfaces.dto.request.TaskUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务应用服务
 * 负责协调领域对象完成任务相关的业务用例
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskApplicationService {

    private final TaskRepository taskRepository;
    private final TaskAssembler taskAssembler;
    private final CacheUtil cacheUtil;
    // TODO: 后续集成事件发布
    // private final TaskEventPublisher taskEventPublisher;

    /**
     * 创建任务
     * 
     * @param request 创建任务请求
     * @param publisherId 发布者ID
     * @return 任务ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(TaskCreateRequest request, Long publisherId) {
        log.info("创建任务: publisherId={}, title={}", publisherId, request.getTitle());

        // 创建任务领域模型
        Task task = Task.create(
                request.getTitle(),
                request.getDescription(),
                request.getRewardAmount(),
                request.getCategoryId(),
                request.getDeadline(),
                request.getMaxAcceptors(),
                publisherId
        );

        // 保存任务
        Task savedTask = taskRepository.save(task);
        
        log.info("任务创建成功: taskId={}", savedTask.getTaskId());
        
        // TODO: 发布任务创建事件
        // taskEventPublisher.publishTaskCreated(savedTask);
        
        return savedTask.getTaskId();
    }

    /**
     * 更新任务
     * 只有待审核或审核拒绝状态的任务可以编辑
     * 
     * @param taskId 任务ID
     * @param request 更新任务请求
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTask(Long taskId, TaskUpdateRequest request, Long userId) {
        log.info("更新任务: taskId={}, userId={}", taskId, userId);

        // 查询任务
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // 验证权限
        if (!task.canBeEditedBy(userId)) {
            if (!task.getPublisherId().equals(userId)) {
                throw new TaskAccessDeniedException("无权编辑该任务");
            }
            throw new TaskInvalidStatusException("当前任务状态不允许编辑");
        }

        // 更新字段
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getRewardAmount() != null) {
            task.setRewardAmount(request.getRewardAmount());
            // 重新计算保证金
            task.setDepositAmount(request.getRewardAmount() / 2);
        }
        if (request.getCategoryId() != null) {
            task.setCategoryId(request.getCategoryId());
        }
        if (request.getDeadline() != null) {
            task.setDeadline(request.getDeadline());
        }
        if (request.getMaxAcceptors() != null) {
            task.setMaxAcceptors(request.getMaxAcceptors());
        }

        // 保存更新
        taskRepository.save(task);
        
        log.info("任务更新成功: taskId={}", taskId);
        
        // 删除任务详情缓存
        invalidateTaskCache(taskId);
    }

    /**
     * 取消任务
     * 只有招募中、待支付、支付中状态的任务可以取消
     * 
     * @param taskId 任务ID
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelTask(Long taskId, Long userId) {
        log.info("取消任务: taskId={}, userId={}", taskId, userId);

        // 查询任务
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // 验证权限
        if (!task.canBeCancelledBy(userId)) {
            if (!task.getPublisherId().equals(userId)) {
                throw new TaskAccessDeniedException("无权取消该任务");
            }
            throw new TaskInvalidStatusException("当前任务状态不允许取消");
        }

        // 取消任务
        task.cancel();
        taskRepository.save(task);
        
        log.info("任务取消成功: taskId={}", taskId);
        
        // 删除任务详情缓存和大厅列表缓存
        invalidateTaskCache(taskId);
        invalidateHallCache();
        
        // TODO: 发布任务取消事件
        // taskEventPublisher.publishTaskCancelled(task);
    }

    /**
     * 获取任务详情
     * 缓存策略：30分钟 + 10-30分钟随机过期时间
     * 
     * @param taskId 任务ID
     * @return 任务DTO
     */
    public TaskDTO getTaskDetail(Long taskId) {
        log.debug("获取任务详情: taskId={}", taskId);

        String cacheKey = RedisKeyConstants.TASK_DETAIL + taskId;
        
        // 使用缓存（30分钟 + 10-30分钟随机）
        return cacheUtil.getOrComputeWithRandomExpire(
                cacheKey,
                TaskDTO.class,
                () -> {
                    Task task = taskRepository.findById(taskId)
                            .orElseThrow(() -> new TaskNotFoundException(taskId));
                    return taskAssembler.toTaskDTO(task);
                },
                30
        );
    }

    /**
     * 分页查询任务大厅列表
     * 只返回招募中状态的任务
     * 缓存策略：5分钟 + 10-30分钟随机过期时间
     * 
     * @param request 查询请求
     * @return 任务列表
     */
    public Page<TaskDTO> pageHallTasks(TaskHallQueryRequest request) {
        log.debug("查询任务大厅: categoryId={}, minReward={}, maxReward={}", 
                request.getCategoryId(), request.getMinReward(), request.getMaxReward());

        // 构建缓存键
        String cacheKey = String.format("%s:%s:%s:%s:%s:%s:%s",
                RedisKeyConstants.TASK_HALL,
                request.getCategoryId() != null ? request.getCategoryId() : "all",
                request.getMinReward() != null ? request.getMinReward() : "0",
                request.getMaxReward() != null ? request.getMaxReward() : "max",
                request.getSortBy() != null ? request.getSortBy() : "created",
                request.getSortOrder() != null ? request.getSortOrder() : "desc",
                request.getPageNum());

        // 注意：MyBatis Plus 的 Page 对象不能直接序列化到 Redis
        // 这里我们只缓存第一页的数据，其他页直接查询数据库
        if (request.getPageNum() > 1) {
            return queryHallTasksFromDb(request);
        }

        // 使用缓存（5分钟 + 10-30分钟随机）
        return cacheUtil.getOrComputeWithRandomExpire(
                cacheKey,
                Page.class,
                () -> queryHallTasksFromDb(request),
                5
        );
    }

    /**
     * 从数据库查询任务大厅列表
     */
    private Page<TaskDTO> queryHallTasksFromDb(TaskHallQueryRequest request) {
        // 转换排序字段
        String sortBy = convertSortBy(request.getSortBy());
        String sortOrder = request.getSortOrder() != null ? request.getSortOrder() : "desc";

        // 查询数据库
        Page<Task> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<Task> taskPage = taskRepository.findHallTasks(
                request.getCategoryId(),
                request.getMinReward(),
                request.getMaxReward(),
                sortBy,
                sortOrder,
                page
        );

        // 转换为DTO
        Page<TaskDTO> result = new Page<>(taskPage.getCurrent(), taskPage.getSize(), taskPage.getTotal());
        List<TaskDTO> dtoList = taskPage.getRecords().stream()
                .map(taskAssembler::toTaskDTO)
                .collect(Collectors.toList());
        result.setRecords(dtoList);

        return result;
    }

    /**
     * 分页查询我发布的任务列表
     * 
     * @param publisherId 发布者ID
     * @param status 任务状态（可选）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 任务列表
     */
    public Page<TaskDTO> pagePublishedTasks(Long publisherId, TaskStatus status, 
                                             Integer pageNum, Integer pageSize) {
        log.debug("查询我发布的任务: publisherId={}, status={}", publisherId, status);

        Page<Task> page = new Page<>(pageNum, pageSize);
        Page<Task> taskPage = taskRepository.findByPublisherId(publisherId, status, page);

        // 转换为DTO
        Page<TaskDTO> result = new Page<>(taskPage.getCurrent(), taskPage.getSize(), taskPage.getTotal());
        List<TaskDTO> dtoList = taskPage.getRecords().stream()
                .map(taskAssembler::toTaskDTO)
                .collect(Collectors.toList());
        result.setRecords(dtoList);

        return result;
    }

    // ==================== 私有方法 ====================

    /**
     * 转换排序字段
     */
    private String convertSortBy(String sortBy) {
        if (sortBy == null) {
            return "createdAt";
        }
        return switch (sortBy) {
            case "reward" -> "rewardAmount";
            case "deadline" -> "deadline";
            case "created" -> "createdAt";
            default -> "createdAt";
        };
    }

    /**
     * 删除任务详情缓存
     */
    private void invalidateTaskCache(Long taskId) {
        String cacheKey = RedisKeyConstants.TASK_DETAIL + taskId;
        cacheUtil.delete(cacheKey);
        log.debug("删除任务详情缓存: taskId={}", taskId);
    }

    /**
     * 删除任务大厅列表缓存（模糊匹配）
     */
    private void invalidateHallCache() {
        String pattern = RedisKeyConstants.TASK_HALL + "*";
        long count = cacheUtil.deleteByPattern(pattern);
        log.debug("删除任务大厅列表缓存: count={}", count);
    }
}
