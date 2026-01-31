package com.universe.life.task.privacy.interfaces.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.auth.common.domain.Result;
import com.universe.life.common.domain.PageResult;
import com.universe.life.task.privacy.application.assembler.TaskAssembler;
import com.universe.life.task.privacy.application.dto.TaskCategoryDTO;
import com.universe.life.task.privacy.application.dto.TaskDTO;
import com.universe.life.task.privacy.application.service.TaskApplicationService;
import com.universe.life.task.privacy.application.service.TaskCategoryService;
import com.universe.life.task.privacy.interfaces.dto.request.TaskCreateRequest;
import com.universe.life.task.privacy.interfaces.dto.request.TaskHallQueryRequest;
import com.universe.life.task.privacy.interfaces.dto.request.TaskUpdateRequest;
import com.universe.life.task.privacy.interfaces.vo.TaskCategoryVO;
import com.universe.life.task.privacy.interfaces.vo.TaskFullDetailVO;
import com.universe.life.task.privacy.interfaces.vo.TaskHallSummaryVO;
import com.universe.life.task.privacy.interfaces.vo.TaskPublishedSummaryVO;
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
 * 任务用户端接口
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Slf4j
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name = "任务管理", description = "任务相关接口")
public class TaskController {

    private final TaskApplicationService taskApplicationService;
    private final TaskCategoryService taskCategoryService;
    private final TaskAssembler taskAssembler;

    /**
     * 任务大厅列表
     */
    @GetMapping("/hall")
    @Operation(summary = "任务大厅列表", description = "分页查询任务大厅列表，只显示招募中的任务")
    public Result<PageResult<TaskHallSummaryVO>> getHallTasks(@Valid TaskHallQueryRequest request) {
        log.info("查询任务大厅: categoryId={}, minReward={}, maxReward={}", 
                request.getCategoryId(), request.getMinReward(), request.getMaxReward());

        Page<TaskDTO> page = taskApplicationService.pageHallTasks(request);
        
        List<TaskHallSummaryVO> voList = page.getRecords().stream()
                .map(taskAssembler::toTaskHallSummaryVO)
                .collect(Collectors.toList());
        
        PageResult<TaskHallSummaryVO> result = PageResult.of(voList, page);
        return Result.success(result);
    }

    /**
     * 任务详情
     */
    @GetMapping("/{taskId}")
    @Operation(summary = "任务详情", description = "获取任务详细信息")
    public Result<TaskFullDetailVO> getTaskDetail(
            @Parameter(description = "任务ID", required = true)
            @PathVariable @NotNull Long taskId) {
        log.info("获取任务详情: taskId={}", taskId);

        TaskDTO taskDTO = taskApplicationService.getTaskDetail(taskId);
        TaskFullDetailVO vo = taskAssembler.toTaskFullDetailVO(taskDTO);
        
        // TODO: 补充发布者信息、审核记录、权限判断等
        
        return Result.success(vo);
    }

    /**
     * 我发布的任务列表
     */
    @GetMapping("/published")
    @Operation(summary = "我发布的任务", description = "查询当前用户发布的任务列表")
    public Result<PageResult<TaskPublishedSummaryVO>> getPublishedTasks(
            @Parameter(description = "任务状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        // TODO: 从SecurityContext获取当前用户ID
        Long publisherId = 1L; // 临时硬编码
        
        log.info("查询我发布的任务: publisherId={}, status={}", publisherId, status);

        TaskStatus taskStatus = status != null ? TaskStatus.of(status) : null;
        Page<TaskDTO> page = taskApplicationService.pagePublishedTasks(publisherId, taskStatus, pageNum, pageSize);
        
        List<TaskPublishedSummaryVO> voList = page.getRecords().stream()
                .map(taskAssembler::toTaskPublishedSummaryVO)
                .collect(Collectors.toList());
        
        PageResult<TaskPublishedSummaryVO> result = PageResult.of(voList, page);
        return Result.success(result);
    }

    /**
     * 创建任务
     */
    @PostMapping
    @Operation(summary = "创建任务", description = "发布新任务")
    public Result<Long> createTask(@Valid @RequestBody TaskCreateRequest request) {
        // TODO: 从SecurityContext获取当前用户ID
        Long publisherId = 1L; // 临时硬编码
        
        log.info("创建任务: publisherId={}, title={}", publisherId, request.getTitle());

        Long taskId = taskApplicationService.createTask(request, publisherId);
        return Result.success(taskId);
    }

    /**
     * 更新任务
     */
    @PutMapping("/{taskId}")
    @Operation(summary = "更新任务", description = "更新任务信息（仅待审核或审核拒绝状态可编辑）")
    public Result<Void> updateTask(
            @Parameter(description = "任务ID", required = true)
            @PathVariable @NotNull Long taskId,
            @Valid @RequestBody TaskUpdateRequest request) {
        
        // TODO: 从SecurityContext获取当前用户ID
        Long userId = 1L; // 临时硬编码
        
        log.info("更新任务: taskId={}, userId={}", taskId, userId);

        taskApplicationService.updateTask(taskId, request, userId);
        return Result.success();
    }

    /**
     * 取消任务
     */
    @PostMapping("/{taskId}/cancel")
    @Operation(summary = "取消任务", description = "取消任务（仅招募中、待支付、支付中状态可取消）")
    public Result<Void> cancelTask(
            @Parameter(description = "任务ID", required = true)
            @PathVariable @NotNull Long taskId) {
        
        // TODO: 从SecurityContext获取当前用户ID
        Long userId = 1L; // 临时硬编码
        
        log.info("取消任务: taskId={}, userId={}", taskId, userId);

        taskApplicationService.cancelTask(taskId, userId);
        return Result.success();
    }

    /**
     * 分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "任务分类列表", description = "获取所有启用的任务分类")
    public Result<List<TaskCategoryVO>> getCategories() {
        log.info("查询任务分类列表");

        List<TaskCategoryDTO> categories = taskCategoryService.listCategories();
        List<TaskCategoryVO> voList = categories.stream()
                .map(dto -> {
                    TaskCategoryVO vo = new TaskCategoryVO();
                    vo.setCategoryId(dto.getCategoryId());
                    vo.setName(dto.getName());
                    vo.setCode(dto.getCode());
                    vo.setDescription(null); // TaskCategoryDTO doesn't have description field
                    vo.setSortOrder(dto.getSort());
                    return vo;
                })
                .collect(Collectors.toList());
        
        return Result.success(voList);
    }
}
