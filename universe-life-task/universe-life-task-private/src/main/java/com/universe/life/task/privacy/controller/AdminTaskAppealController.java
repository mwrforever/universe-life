package com.universe.life.task.privacy.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.common.result.PageResult;
import com.universe.life.task.privacy.domain.dao.query.TaskAppealQuery;
import com.universe.life.task.privacy.domain.dto.request.TaskAppealHandleRequest;
import com.universe.life.task.privacy.domain.vo.TaskAppealStatsVO;
import com.universe.life.task.privacy.domain.vo.TaskAppealVO;
import com.universe.life.task.privacy.service.IAdminTaskAppealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端申诉控制器
 */
@Tag(name = "管理端-申诉处理")
@RestController
@RequestMapping("/admin/task-appeals")
@RequiredArgsConstructor
@Validated
public class AdminTaskAppealController {

    private final IAdminTaskAppealService appealService;

    @GetMapping
    @Operation(summary = "申诉列表")
    public Result<PageResult<TaskAppealVO>> pageAppeals(TaskAppealQuery query) {
        return Result.success(appealService.pageAppeals(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "申诉详情", description = "获取申诉详细信息")
    public Result<TaskAppealVO> getAppealDetail(@Parameter(description = "申诉ID", required = true) @PathVariable Long id) {
        return Result.success(appealService.getAppealDetail(id));
    }

    @PostMapping("/{id}/handle")
    @Operation(summary = "处理申诉", description = "处理指定申诉")
    public Result<Void> handleAppeal(@Parameter(description = "申诉ID", required = true) @PathVariable Long id, 
                                     @RequestBody @Validated TaskAppealHandleRequest request) {
        appealService.handleAppeal(id, request, SecurityUtil.getUserId());
        return Result.success();
    }

    @GetMapping("/stats")
    @Operation(summary = "申诉统计")
    public Result<TaskAppealStatsVO> getAppealStats() {
        return Result.success(appealService.getAppealStats());
    }
}
