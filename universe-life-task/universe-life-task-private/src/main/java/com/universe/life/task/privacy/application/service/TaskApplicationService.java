package com.universe.life.task.privacy.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.common.util.CacheUtil;
import com.universe.life.task.privacy.application.event.TaskEventPublisher;
import com.universe.life.task.privacy.infrastructure.enums.TaskStatus;
import com.universe.life.task.privacy.application.assembler.TaskAssembler;
import com.universe.life.task.privacy.application.dto.TaskDTO;
import com.universe.life.task.privacy.domain.exception.TaskAccessDeniedException;
import com.universe.life.task.privacy.domain.exception.TaskInvalidStatusException;
import com.universe.life.task.privacy.domain.exception.TaskNotFoundException;
import com.universe.life.task.privacy.domain.model.Task;
import com.universe.life.task.privacy.domain.repository.TaskRepository;
import com.universe.life.task.privacy.application.command.CancelTaskCommand;
import com.universe.life.task.privacy.application.command.CreateTaskCommand;
import com.universe.life.task.privacy.application.command.UpdateTaskCommand;
import com.universe.life.task.privacy.application.query.TaskHallQuery;
import com.universe.life.task.privacy.infrastructure.constants.RedisKeyConstants;
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
    private final TaskEventPublisher taskEventPublisher;

    /**
     * 创建任务
     *
     * @param command 创建任务命令
     * @param publisherId 发布者ID
     * @return 任务ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(CreateTaskCommand command, Long publisherId) {
        log.info("创建任务: publisherId={}, title={}", publisherId, command.getTitle());

        // 创建任务领域模型
        Task task = Task.create(
                command.getTitle(),
                command.getDescription(),
                command.getRewardAmount(),
                command.getCategoryId(),
                command.getDeadline(),
                command.getMaxAcceptors(),
                publisherId
        );

        // 保存任务
        Task savedTask = taskRepository.save(task);
        
        log.info("任务创建成功: taskId={}", savedTask.getTaskId());

        // 发布任务创建事件
        taskEventPublisher.publishTaskCreated(savedTask);

        return savedTask.getTaskId();
    }

    /**
     * 更新任务
     * 只有待审核或审核拒绝状态的任务可以编辑
     *
     * @param taskId 任务ID
     * @param command 更新任务命令
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTask(Long taskId, UpdateTaskCommand command, Long userId) {
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
        if (command.getTitle() != null) {
            task.setTitle(command.getTitle());
        }
        if (command.getDescription() != null) {
            task.setDescription(command.getDescription());
        }
        if (command.getRewardAmount() != null) {
            task.setRewardAmount(command.getRewardAmount());
            // 重新计算保证金
            task.setDepositAmount(command.getRewardAmount() / 2);
        }
        if (command.getCategoryId() != null) {
            task.setCategoryId(command.getCategoryId());
        }
        if (command.getDeadline() != null) {
            task.setDeadline(command.getDeadline());
        }
        if (command.getMaxAcceptors() != null) {
            task.setMaxAcceptors(command.getMaxAcceptors());
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
     * @param command 取消任务命令
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelTask(CancelTaskCommand command) {
        log.info("取消任务: taskId={}, userId={}", command.getTaskId(), command.getOperatorId());

        // 查询任务
        Task task = taskRepository.findById(command.getTaskId())
                .orElseThrow(() -> new TaskNotFoundException(command.getTaskId()));

        // 验证权限
        if (!task.canBeCancelledBy(command.getOperatorId())) {
            if (!task.getPublisherId().equals(command.getOperatorId())) {
                throw new TaskAccessDeniedException("无权取消该任务");
            }
            throw new TaskInvalidStatusException("当前任务状态不允许取消");
        }

        // 取消任务
        task.cancel();
        taskRepository.save(task);

        log.info("任务取消成功: taskId={}", command.getTaskId());

        // 删除任务详情缓存和大厅列表缓存
        invalidateTaskCache(command.getTaskId());
        invalidateHallCache();

        // 发布任务取消事件
        String reason = command.getReason() != null ? command.getReason() : "用户主动取消";
        taskEventPublisher.publishTaskCancelled(task, reason);
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
     * 缓存策略：使用Hash结构，key为task:hall，field为查询条件组合
     *
     * @param query 查询对象
     * @return 任务列表
     */
    public Page<TaskDTO> pageHallTasks(TaskHallQuery query) {
        log.debug("查询任务大厅: categoryId={}, minReward={}, maxReward={}",
                query.getCategoryId(), query.getMinReward(), query.getMaxReward());

        // 注意：MyBatis Plus 的 Page 对象不能直接序列化到 Redis
        // 这里我们只缓存第一页的数据，其他页直接查询数据库
        if (query.getPageNum() != null && query.getPageNum() > 1) {
            return queryHallTasksFromDb(query);
        }

        // 构建Hash的field
        String hashField = buildHallCacheField(query);

        // 使用Hash缓存（5分钟 + 10-30分钟随机）
        Page<TaskDTO> cachedPage = cacheUtil.hGet(
                RedisKeyConstants.TASK_HALL,
                hashField,
                Page.class
        );

        if (cachedPage != null) {
            return cachedPage;
        }

        Page<TaskDTO> computed = queryHallTasksFromDb(query);
        // 添加10-30分钟的随机过期时间
        int randomMinutes = 10 + (int) (Math.random() * 21);
        cacheUtil.hSet(RedisKeyConstants.TASK_HALL, hashField, computed, 5 + randomMinutes);
        return computed;
    }

    /**
     * 构建任务大厅缓存的Hash field
     */
    private String buildHallCacheField(TaskHallQuery query) {
        return String.format("%s:%s:%s:%s:%s:1",
                query.getCategoryId() != null ? query.getCategoryId() : "all",
                query.getMinReward() != null ? query.getMinReward() : "0",
                query.getMaxReward() != null ? query.getMaxReward() : "max",
                query.getSortBy() != null ? query.getSortBy() : "created",
                query.getSortOrder() != null ? query.getSortOrder() : "desc");
    }

    /**
     * 从数据库查询任务大厅列表
     */
    private Page<TaskDTO> queryHallTasksFromDb(TaskHallQuery query) {
        // 转换排序字段
        String sortBy = convertSortBy(query.getSortBy());
        String sortOrder = query.getSortOrder() != null ? query.getSortOrder() : "desc";

        // 查询数据库
        Page<Task> page = new Page<>(query.getPageNumOrDefault(), query.getPageSizeOrDefault());
        Page<Task> taskPage = taskRepository.findHallTasks(
                query.getCategoryId(),
                query.getMinReward(),
                query.getMaxReward(),
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
     * 删除任务大厅列表缓存（删除整个Hash）
     */
    private void invalidateHallCache() {
        cacheUtil.hDeleteAll(RedisKeyConstants.TASK_HALL);
        log.debug("删除任务大厅列表缓存: key={}", RedisKeyConstants.TASK_HALL);
    }
}
