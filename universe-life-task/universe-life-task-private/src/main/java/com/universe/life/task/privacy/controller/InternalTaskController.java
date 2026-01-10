package com.universe.life.task.privacy.controller;

import com.universe.life.task.model.dto.TaskInfoDTO;
import com.universe.life.task.model.enums.TaskStatus;
import com.universe.life.task.privacy.domain.po.Task;
import com.universe.life.task.privacy.mapper.TaskMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 内部任务接口（供其他服务调用）
 *
 * @author universe-life
 */
@Tag(name = "内部任务接口", description = "供其他服务调用的内部接口")
@RestController
@RequestMapping("/internal/task")
@RequiredArgsConstructor
public class InternalTaskController {

    private final TaskMapper taskMapper;

    @Operation(summary = "获取任务信息")
    @GetMapping("/{taskId}")
    public TaskInfoDTO getTaskInfo(@PathVariable Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            return null;
        }
        TaskInfoDTO dto = new TaskInfoDTO();
        dto.setId(task.getId());
        dto.setPublisherId(task.getPublisherId());
        dto.setTitle(task.getTitle());
        dto.setRewardAmount(task.getRewardAmount());
        // 将内部TaskStatus转换为公共TaskStatus
        dto.setStatus(TaskStatus.ofCode(task.getStatus().getCode()));
        dto.setMaxAcceptors(task.getMaxAcceptors());
        dto.setCurrentAcceptors(task.getCurrentAcceptors());
        dto.setDeadline(task.getDeadline());
        return dto;
    }

    @Operation(summary = "更新任务状态")
    @PutMapping("/{taskId}/status")
    public void updateTaskStatus(@PathVariable Long taskId, @RequestParam TaskStatus status) {
        Task task = taskMapper.selectById(taskId);
        if (task != null) {
            // 将公共TaskStatus转换为内部TaskStatus
            task.setStatus(com.universe.life.task.privacy.enums.TaskStatus.ofCode(status.getCode()));
            taskMapper.updateById(task);
        }
    }

    @Operation(summary = "增加当前接单人数")
    @PutMapping("/{taskId}/acceptors/increase")
    public void increaseAcceptors(@PathVariable Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task != null) {
            task.setCurrentAcceptors(task.getCurrentAcceptors() + 1);
            taskMapper.updateById(task);
        }
    }

    @Operation(summary = "减少当前接单人数")
    @PutMapping("/{taskId}/acceptors/decrease")
    public void decreaseAcceptors(@PathVariable Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task != null && task.getCurrentAcceptors() > 0) {
            task.setCurrentAcceptors(task.getCurrentAcceptors() - 1);
            taskMapper.updateById(task);
        }
    }

    @Operation(summary = "检查用户是否为任务发布者")
    @GetMapping("/{taskId}/publisher/check")
    public Boolean isPublisher(@PathVariable Long taskId, @RequestParam Long userId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            return false;
        }
        return task.getPublisherId().equals(userId);
    }
}
