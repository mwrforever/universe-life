package com.universe.life.task.privacy.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.common.util.CacheUtil;
import com.universe.life.task.privacy.domain.exception.TaskInvalidStatusException;
import com.universe.life.task.privacy.domain.exception.TaskNotFoundException;
import com.universe.life.task.privacy.domain.model.Task;
import com.universe.life.task.privacy.domain.repository.TaskRepository;
import com.universe.life.task.privacy.domain.repository.TaskReviewRecordRepository;
import com.universe.life.task.privacy.infrastructure.constants.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 任务审核服务
 * 负责处理任务审核流程
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskReviewService {

    private final TaskRepository taskRepository;
    private final TaskReviewRecordRepository taskReviewRecordRepository;
    private final CacheUtil cacheUtil;
    // TODO: 后续集成事件发布
    // private final TaskEventPublisher taskEventPublisher;

    /**
     * 审核通过
     * 将任务状态从 PENDING 变更为 RECRUITING
     * 
     * @param taskId 任务ID
     * @param reviewerId 审核人ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveTask(Long taskId, Long reviewerId) {
        log.info("审核通过任务: taskId={}, reviewerId={}", taskId, reviewerId);

        // 查询任务
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // 验证任务状态
        if (!task.getStatus().isPending()) {
            throw new TaskInvalidStatusException("只有待审核状态的任务才能审核");
        }

        // 审核通过
        task.approve();
        taskRepository.save(task);

        // 创建审核记录
        taskReviewRecordRepository.createRecord(
                taskId,
                reviewerId,
                TaskReviewStatus.APPROVED,
                null,
                LocalDateTime.now()
        );

        log.info("任务审核通过成功: taskId={}", taskId);

        // 删除任务详情缓存和大厅列表缓存
        invalidateTaskCache(taskId);
        invalidateHallCache();

        // TODO: 发布任务审核通过事件
        // taskEventPublisher.publishTaskApproved(task);
    }

    /**
     * 审核拒绝
     * 将任务状态从 PENDING 变更为 REJECTED
     * 
     * @param taskId 任务ID
     * @param reviewerId 审核人ID
     * @param rejectReason 拒绝原因
     */
    @Transactional(rollbackFor = Exception.class)
    public void rejectTask(Long taskId, Long reviewerId, String rejectReason) {
        log.info("审核拒绝任务: taskId={}, reviewerId={}, reason={}", taskId, reviewerId, rejectReason);

        // 验证拒绝原因
        if (rejectReason == null || rejectReason.trim().isEmpty()) {
            throw new IllegalArgumentException("拒绝原因不能为空");
        }

        // 查询任务
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // 验证任务状态
        if (!task.getStatus().isPending()) {
            throw new TaskInvalidStatusException("只有待审核状态的任务才能审核");
        }

        // 审核拒绝
        task.reject();
        taskRepository.save(task);

        // 创建审核记录
        taskReviewRecordRepository.createRecord(
                taskId,
                reviewerId,
                TaskReviewStatus.REJECTED,
                rejectReason,
                LocalDateTime.now()
        );

        log.info("任务审核拒绝成功: taskId={}", taskId);

        // 删除任务详情缓存
        invalidateTaskCache(taskId);

        // TODO: 发布任务审核拒绝事件
        // taskEventPublisher.publishTaskRejected(task, rejectReason);
    }

    /**
     * 分页查询待审核任务列表
     * 只返回状态为 PENDING 的任务
     * 
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 待审核任务列表
     */
    public Page<Task> pagePendingTasks(Integer pageNum, Integer pageSize) {
        log.debug("查询待审核任务列表: pageNum={}, pageSize={}", pageNum, pageSize);

        Page<Task> page = new Page<>(pageNum, pageSize);
        return taskRepository.findByStatus(TaskStatus.PENDING, page);
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
     * 删除任务大厅列表缓存（模糊匹配）
     */
    private void invalidateHallCache() {
        String pattern = RedisKeyConstants.TASK_HALL + "*";
        long count = cacheUtil.deleteByPattern(pattern);
        log.debug("删除任务大厅列表缓存: count={}", count);
    }
}
