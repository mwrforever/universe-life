package com.universe.life.task.privacy.interfaces.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.task.model.dto.TaskInfoDTO;
import com.universe.life.task.model.enums.TaskStatus;
import com.universe.life.task.privacy.domain.model.aggregate.Task;
import com.universe.life.task.privacy.domain.repository.TaskRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 内部任务接口控制器
 * <p>
 * 供其他微服务调用的内部接口，主要用于：
 * <ul>
 *   <li>Trade服务获取任务信息</li>
 *   <li>Trade服务更新任务接单人数</li>
 *   <li>其他服务验证任务发布者</li>
 * </ul>
 * </p>
 * <p>
 * 注意：这些接口仅供内部服务调用，不对外暴露。
 * 应通过网关配置或服务间认证来限制访问。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Tag(name = "内部任务接口", description = "供其他服务调用的内部接口")
@RestController
@RequestMapping("/internal/task")
@RequiredArgsConstructor
public class InternalTaskController {

    /** 任务仓储 */
    private final TaskRepository taskRepository;

    /**
     * 获取任务信息
     * <p>
     * 返回任务的基本信息，供Trade服务在接单时验证任务状态。
     * </p>
     *
     * @param taskId 任务ID
     * @return 任务信息DTO，任务不存在时返回null
     */
    @GetMapping("/{taskId}")
    @Operation(summary = "获取任务信息", description = "获取任务基本信息，供其他服务调用")
    public Result<TaskInfoDTO> getTaskInfo(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId) {
        // 1. 查询任务
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return Result.success(null);
        }
        
        // 2. 转换为DTO
        Task task = taskOpt.get();
        TaskInfoDTO dto = new TaskInfoDTO();
        dto.setId(task.getTaskId());
        dto.setPublisherId(task.getPublisherId());
        dto.setTitle(task.getTitle());
        dto.setRewardAmount(task.getRewardAmount());
        // 将领域层状态枚举转换为公共状态枚举
        dto.setStatus(TaskStatus.ofCode(task.getStatus().getCode()));
        dto.setMaxAcceptors(task.getMaxAcceptors());
        dto.setCurrentAcceptors(task.getCurrentAcceptors());
        dto.setDeadline(task.getDeadline());
        
        return Result.success(dto);
    }

    /**
     * 检查任务是否可接单
     * <p>
     * 验证任务是否处于招募中状态且接单人数未满。
     * </p>
     *
     * @param taskId 任务ID
     * @return 是否可接单
     */
    @GetMapping("/{taskId}/can-accept")
    @Operation(summary = "检查任务是否可接单", description = "验证任务状态和接单人数")
    public Result<Boolean> canAcceptTask(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return Result.success(false);
        }
        Task task = taskOpt.get();
        // 检查任务是否可以接单：招募中状态、未过期、未达到接单人数上限
        boolean canAccept = task.getStatus().isRecruiting() 
                && !task.isExpired() 
                && !task.isAcceptorLimitReached();
        return Result.success(canAccept);
    }

    /**
     * 增加任务接单人数
     * <p>
     * 当用户成功接单时调用，增加当前接单人数。
     * </p>
     *
     * @param taskId 任务ID
     * @return 操作结果
     */
    @PostMapping("/{taskId}/increment-acceptors")
    @Operation(summary = "增加接单人数", description = "接单成功时调用")
    public Result<Void> incrementAcceptors(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));
        task.incrementAcceptorCount();
        taskRepository.save(task);
        return Result.success();
    }

    /**
     * 减少任务接单人数
     * <p>
     * 当用户取消接单或被拒绝时调用，减少当前接单人数。
     * </p>
     *
     * @param taskId 任务ID
     * @return 操作结果
     */
    @PostMapping("/{taskId}/decrement-acceptors")
    @Operation(summary = "减少接单人数", description = "取消接单或被拒绝时调用")
    public Result<Void> decrementAcceptors(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));
        task.decrementAcceptorCount();
        taskRepository.save(task);
        return Result.success();
    }

    /**
     * 检查用户是否为任务发布者
     * <p>
     * 验证指定用户是否为任务的发布者。
     * </p>
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 是否为发布者
     */
    @GetMapping("/{taskId}/is-publisher")
    @Operation(summary = "检查是否为发布者", description = "验证用户是否为任务发布者")
    public Result<Boolean> isPublisher(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId,
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return Result.success(false);
        }
        return Result.success(taskOpt.get().getPublisherId().equals(userId));
    }
}
