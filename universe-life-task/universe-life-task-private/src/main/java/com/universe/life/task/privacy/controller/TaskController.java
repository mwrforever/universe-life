package com.universe.life.task.privacy.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.common.domain.PageResult;
import com.universe.life.task.privacy.domain.dao.query.TaskQuery;
import com.universe.life.task.privacy.domain.dto.request.TaskCreateRequest;
import com.universe.life.task.privacy.domain.dto.request.TaskDepositRequest;
import com.universe.life.task.privacy.domain.dto.request.TaskUpdateRequest;
import com.universe.life.task.privacy.domain.vo.TaskCategoryVO;
import com.universe.life.task.privacy.domain.vo.TaskDepositVO;
import com.universe.life.task.privacy.domain.vo.TaskDetailVO;
import com.universe.life.task.privacy.domain.vo.TaskVO;
import com.universe.life.task.privacy.service.ITaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务管理控制器
 */
@Tag(name = "任务管理")
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Validated
public class TaskController {

    private final ITaskService taskService;

    @PostMapping
    @Operation(summary = "发布任务")
    public Result<Long> createTask(@RequestBody @Validated TaskCreateRequest request) {
        return Result.success(taskService.createTask(request, SecurityUtil.getUserId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑任务", description = "编辑指定任务信息")
    public Result<Void> updateTask(@Parameter(description = "任务ID", required = true) @PathVariable Long id, 
                                   @RequestBody @Validated TaskUpdateRequest request) {
        taskService.updateTask(id, request, SecurityUtil.getUserId());
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "任务详情", description = "获取任务详细信息")
    public Result<TaskDetailVO> getTaskDetail(@Parameter(description = "任务ID", required = true) @PathVariable Long id) {
        return Result.success(taskService.getTaskDetail(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消任务", description = "取消指定任务")
    public Result<Void> cancelTask(@Parameter(description = "任务ID", required = true) @PathVariable Long id) {
        taskService.cancelTask(id, SecurityUtil.getUserId());
        return Result.success();
    }

    @PostMapping("/{id}/deposit")
    @Operation(summary = "支付保证金", description = "为指定任务支付保证金")
    public Result<TaskDepositVO> payDeposit(@Parameter(description = "任务ID", required = true) @PathVariable Long id,
                                            @RequestBody @Validated TaskDepositRequest request) {
        return Result.success(taskService.payDeposit(id, request, SecurityUtil.getUserId()));
    }

    @GetMapping("/published")
    @Operation(summary = "我发布的任务")
    public Result<PageResult<TaskVO>> pagePublishedTasks(TaskQuery query) {
        return Result.success(taskService.pagePublishedTasks(query, SecurityUtil.getUserId()));
    }

    @GetMapping("/hall")
    @Operation(summary = "任务大厅")
    public Result<PageResult<TaskVO>> pageHallTasks(TaskQuery query) {
        return Result.success(taskService.pageHallTasks(query));
    }

    @GetMapping("/categories")
    @Operation(summary = "任务分类列表")
    public Result<List<TaskCategoryVO>> listCategories() {
        return Result.success(taskService.listCategories());
    }
}
