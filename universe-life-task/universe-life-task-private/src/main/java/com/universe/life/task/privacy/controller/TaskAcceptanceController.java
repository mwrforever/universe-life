package com.universe.life.task.privacy.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.common.domain.PageResult;
import com.universe.life.task.privacy.domain.dao.query.TaskAcceptanceQuery;
import com.universe.life.task.privacy.domain.dto.request.TaskAbandonRequest;
import com.universe.life.task.privacy.domain.dto.request.TaskAppealRequest;
import com.universe.life.task.privacy.domain.dto.request.TaskConfirmRequest;
import com.universe.life.task.privacy.domain.dto.request.TaskRejectRequest;
import com.universe.life.task.privacy.domain.dto.request.TaskSubmitRequest;
import com.universe.life.task.privacy.domain.vo.TaskAcceptanceVO;
import com.universe.life.task.privacy.service.ITaskAcceptanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 任务接受记录控制器
 */
@Tag(name = "任务接受记录")
@RestController
@RequiredArgsConstructor
@Validated
public class TaskAcceptanceController {

    private final ITaskAcceptanceService acceptanceService;

    @PostMapping("/tasks/{taskId}/accept")
    @Operation(summary = "接受任务", description = "接受指定任务")
    public Result<Long> acceptTask(@Parameter(description = "任务ID", required = true) @PathVariable Long taskId) {
        return Result.success(acceptanceService.acceptTask(taskId, SecurityUtil.getUserId()));
    }

    @GetMapping("/task-acceptances")
    @Operation(summary = "我的接受记录")
    public Result<PageResult<TaskAcceptanceVO>> pageMyAcceptances(TaskAcceptanceQuery query) {
        return Result.success(acceptanceService.pageMyAcceptances(query, SecurityUtil.getUserId()));
    }

    @GetMapping("/task-acceptances/task/{taskId}")
    @Operation(summary = "任务的接受记录", description = "发布者查看指定任务的接受记录")
    public Result<PageResult<TaskAcceptanceVO>> pageTaskAcceptances(@Parameter(description = "任务ID", required = true) @PathVariable Long taskId, 
                                                                    TaskAcceptanceQuery query) {
        return Result.success(acceptanceService.pageTaskAcceptances(taskId, query, SecurityUtil.getUserId()));
    }

    @GetMapping("/task-acceptances/{id}")
    @Operation(summary = "接受记录详情", description = "获取接受记录详细信息")
    public Result<TaskAcceptanceVO> getAcceptanceDetail(@Parameter(description = "接受记录ID", required = true) @PathVariable Long id) {
        return Result.success(acceptanceService.getAcceptanceDetail(id, SecurityUtil.getUserId()));
    }

    @PostMapping("/task-acceptances/{id}/submit")
    @Operation(summary = "提交任务成果", description = "提交指定接受记录的任务成果")
    public Result<Void> submitTask(@Parameter(description = "接受记录ID", required = true) @PathVariable Long id, 
                                   @RequestBody @Validated TaskSubmitRequest request) {
        acceptanceService.submitTask(id, request, SecurityUtil.getUserId());
        return Result.success();
    }

    @PostMapping("/task-acceptances/{id}/confirm")
    @Operation(summary = "确认任务完成", description = "发布者确认任务完成")
    public Result<Void> confirmTask(@Parameter(description = "接受记录ID", required = true) @PathVariable Long id, 
                                    @RequestBody @Validated TaskConfirmRequest request) {
        acceptanceService.confirmTask(id, request, SecurityUtil.getUserId());
        return Result.success();
    }

    @PostMapping("/task-acceptances/{id}/abandon")
    @Operation(summary = "放弃任务", description = "接受者放弃任务")
    public Result<Void> abandonTask(@Parameter(description = "接受记录ID", required = true) @PathVariable Long id, 
                                    @RequestBody @Validated TaskAbandonRequest request) {
        acceptanceService.abandonTask(id, request, SecurityUtil.getUserId());
        return Result.success();
    }

    @PostMapping("/task-acceptances/{id}/appeal")
    @Operation(summary = "发起申诉", description = "对指定接受记录发起申诉")
    public Result<Long> initiateAppeal(@Parameter(description = "接受记录ID", required = true) @PathVariable Long id, 
                                       @RequestBody @Validated TaskAppealRequest request) {
        return Result.success(acceptanceService.initiateAppeal(id, request, SecurityUtil.getUserId()));
    }

    @PostMapping("/task-acceptances/{id}/approve")
    @Operation(summary = "同意接单", description = "发布者同意接单者的申请")
    public Result<Void> approveAcceptance(@Parameter(description = "接受记录ID", required = true) @PathVariable Long id) {
        acceptanceService.approveAcceptance(id, SecurityUtil.getUserId());
        return Result.success();
    }

    @PostMapping("/task-acceptances/{id}/reject")
    @Operation(summary = "拒绝接单", description = "发布者拒绝接单者的申请")
    public Result<Void> rejectAcceptance(@Parameter(description = "接受记录ID", required = true) @PathVariable Long id,
                                         @RequestBody @Validated TaskRejectRequest request) {
        acceptanceService.rejectAcceptance(id, request, SecurityUtil.getUserId());
        return Result.success();
    }
}
