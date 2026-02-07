package com.universe.life.task.privacy.interfaces.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.auth.common.domain.Result;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.common.domain.PageResult;
import com.universe.life.task.privacy.application.assembler.TaskAssembler;
import com.universe.life.task.privacy.application.service.TaskReviewService;
import com.universe.life.task.privacy.domain.model.Task;
import com.universe.life.task.privacy.interfaces.assembler.TaskVOAssembler;
import com.universe.life.task.privacy.interfaces.dto.request.TaskRejectRequest;
import com.universe.life.task.privacy.interfaces.vo.TaskFullDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务管理端接口
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Slf4j
@RestController
@RequestMapping("/admin/tasks")
@RequiredArgsConstructor
@Tag(name = "任务管理（管理端）", description = "任务审核相关接口")
public class AdminTaskController {

    private final TaskReviewService taskReviewService;
    private final TaskAssembler taskAssembler;
    private final TaskVOAssembler taskVOAssembler;

    /**
     * 待审核任务列表
     */
    @GetMapping("/pending")
    @Operation(summary = "待审核任务列表", description = "分页查询待审核的任务列表")
    public Result<PageResult<TaskFullDetailVO>> getPendingTasks(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        log.info("查询待审核任务列表: pageNum={}, pageSize={}", pageNum, pageSize);

        Page<Task> page = taskReviewService.pagePendingTasks(pageNum, pageSize);
        
        List<TaskFullDetailVO> voList = page.getRecords().stream()
                .map(task -> taskVOAssembler.toTaskFullDetailVO(taskAssembler.toTaskDTO(task)))
                .collect(Collectors.toList());
        
        PageResult<TaskFullDetailVO> result = PageResult.of(voList, page);
        return Result.success(result);
    }

    /**
     * 审核通过
     */
    @PutMapping("/{taskId}/approve")
    @Operation(summary = "审核通过", description = "审核通过任务")
    public Result<Void> approveTask(
            @Parameter(description = "任务ID", required = true)
            @PathVariable @NotNull Long taskId) {

        Long reviewerId = SecurityUtil.getUserId();

        log.info("审核通过任务: taskId={}, reviewerId={}", taskId, reviewerId);

        taskReviewService.approveTask(taskId, reviewerId);
        return Result.success();
    }

    /**
     * 审核拒绝
     */
    @PutMapping("/{taskId}/reject")
    @Operation(summary = "审核拒绝", description = "审核拒绝任务")
    public Result<Void> rejectTask(
            @Parameter(description = "任务ID", required = true)
            @PathVariable @NotNull Long taskId,
            @Valid @RequestBody TaskRejectRequest request) {

        Long reviewerId = SecurityUtil.getUserId();

        log.info("审核拒绝任务: taskId={}, reviewerId={}, reason={}", taskId, reviewerId, request.getReason());

        taskReviewService.rejectTask(taskId, reviewerId, request.getReason());
        return Result.success();
    }
}
