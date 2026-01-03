package com.universe.life.task.privacy.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.common.domain.PageResult;
import com.universe.life.task.privacy.domain.dao.query.TaskQuery;
import com.universe.life.task.privacy.domain.dto.request.TaskApproveRequest;
import com.universe.life.task.privacy.domain.dto.request.TaskCategoryRequest;
import com.universe.life.task.privacy.domain.dto.request.TaskOfflineRequest;
import com.universe.life.task.privacy.domain.dto.request.TaskRejectRequest;
import com.universe.life.task.privacy.domain.vo.TaskCategoryVO;
import com.universe.life.task.privacy.domain.vo.TaskDetailVO;
import com.universe.life.task.privacy.domain.vo.TaskReviewVO;
import com.universe.life.task.privacy.domain.vo.TaskVO;
import com.universe.life.task.privacy.service.IAdminTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端任务控制器
 */
@Tag(name = "管理端-任务审核")
@RestController
@RequestMapping("/admin/tasks")
@RequiredArgsConstructor
@Validated
public class AdminTaskController {

    private final IAdminTaskService adminTaskService;

    @GetMapping("/pending")
    @Operation(summary = "待审核任务列表")
    public Result<PageResult<TaskVO>> pagePendingTasks(TaskQuery query) {
        return Result.success(adminTaskService.pagePendingTasks(query));
    }

    @GetMapping
    @Operation(summary = "全部任务列表")
    public Result<PageResult<TaskVO>> pageAllTasks(TaskQuery query) {
        return Result.success(adminTaskService.pageAllTasks(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "任务详情", description = "根据任务ID获取任务详细信息")
    public Result<TaskDetailVO> getTaskDetail(@Parameter(description = "任务ID", required = true) @PathVariable Long id) {
        return Result.success(adminTaskService.getTaskDetail(id));
    }

    @GetMapping("/{id}/reviews")
    @Operation(summary = "任务审核记录", description = "获取指定任务的审核历史记录")
    public Result<List<TaskReviewVO>> getTaskReviews(@Parameter(description = "任务ID", required = true) @PathVariable Long id) {
        return Result.success(adminTaskService.getTaskReviews(id));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "审核通过", description = "审核通过指定任务")
    public Result<Void> approveTask(@Parameter(description = "任务ID", required = true) @PathVariable Long id, 
                                    @RequestBody(required = false) TaskApproveRequest request) {
        adminTaskService.approveTask(id, request, SecurityUtil.getUserId());
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "审核拒绝", description = "审核拒绝指定任务")
    public Result<Void> rejectTask(@Parameter(description = "任务ID", required = true) @PathVariable Long id, 
                                   @RequestBody @Validated TaskRejectRequest request) {
        adminTaskService.rejectTask(id, request, SecurityUtil.getUserId());
        return Result.success();
    }

    @PostMapping("/{id}/offline")
    @Operation(summary = "强制下架", description = "强制下架指定任务")
    public Result<Void> offlineTask(@Parameter(description = "任务ID", required = true) @PathVariable Long id, 
                                    @RequestBody @Validated TaskOfflineRequest request) {
        adminTaskService.offlineTask(id, request, SecurityUtil.getUserId());
        return Result.success();
    }

    @GetMapping("/categories")
    @Operation(summary = "分类列表")
    public Result<List<TaskCategoryVO>> listCategories() {
        return Result.success(adminTaskService.listCategories());
    }

    @PostMapping("/categories")
    @Operation(summary = "创建分类")
    public Result<Void> createCategory(@RequestBody @Validated TaskCategoryRequest request) {
        adminTaskService.createCategory(request);
        return Result.success();
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "更新分类", description = "更新指定任务分类")
    public Result<Void> updateCategory(@Parameter(description = "分类ID", required = true) @PathVariable Long id, 
                                       @RequestBody @Validated TaskCategoryRequest request) {
        adminTaskService.updateCategory(id, request);
        return Result.success();
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "删除分类", description = "删除指定任务分类")
    public Result<Void> deleteCategory(@Parameter(description = "分类ID", required = true) @PathVariable Long id) {
        adminTaskService.deleteCategory(id);
        return Result.success();
    }
}
