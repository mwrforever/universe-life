package com.universe.life.task.privacy.interfaces.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.task.privacy.application.assembler.TaskCategoryAssembler;
import com.universe.life.task.privacy.application.dto.TaskCategoryDTO;
import com.universe.life.task.privacy.application.service.TaskCategoryService;
import com.universe.life.task.privacy.infrastructure.enums.CommonStatus;
import com.universe.life.task.privacy.interfaces.dto.request.CreateCategoryRequest;
import com.universe.life.task.privacy.interfaces.dto.request.UpdateCategoryRequest;
import com.universe.life.task.privacy.interfaces.vo.TaskCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务分类管理端接口
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Slf4j
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Tag(name = "任务分类管理（管理端）", description = "任务分类管理接口")
public class AdminCategoryController {

    private final TaskCategoryService taskCategoryService;
    private final TaskCategoryAssembler taskCategoryAssembler;

    /**
     * 分类列表（包含已禁用）
     */
    @GetMapping
    @Operation(summary = "分类列表", description = "获取所有任务分类（包含已禁用）")
    public Result<List<TaskCategoryVO>> getAllCategories(
            @Parameter(description = "状态筛选") @RequestParam(required = false) Integer status) {
        
        log.info("查询所有任务分类: status={}", status);

        CommonStatus commonStatus = CommonStatus.of(status);

        List<TaskCategoryDTO> categories = taskCategoryService.listCategories(commonStatus);
        
        List<TaskCategoryVO> voList = taskCategoryAssembler.toTaskCategoryVO(categories);
        
        return Result.success(voList);
    }

    /**
     * 创建分类
     */
    @PostMapping
    @Operation(summary = "创建分类", description = "创建新的任务分类")
    public Result<Long> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        log.info("创建任务分类: name={}, code={}", request.getName(), request.getCode());

        Long categoryId = taskCategoryService.createCategory(
                request.getName(),
                request.getCode(),
                request.getSortOrder()
        );
        
        return Result.success(categoryId);
    }

    /**
     * 更新分类
     */
    @PutMapping("/{categoryId}")
    @Operation(summary = "更新分类", description = "更新任务分类信息")
    public Result<Void> updateCategory(
            @Parameter(description = "分类ID", required = true)
            @PathVariable @NotNull Long categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        
        log.info("更新任务分类: categoryId={}, name={}", categoryId, request.getName());

        taskCategoryService.updateCategory(
                categoryId,
                request.getName(),
                request.getSortOrder()
        );
        
        return Result.success();
    }

    /**
     * 禁用分类
     */
    @PutMapping("/{categoryId}/disable")
    @Operation(summary = "禁用分类", description = "禁用任务分类")
    public Result<Void> disableCategory(
            @Parameter(description = "分类ID", required = true)
            @PathVariable @NotNull Long categoryId) {
        
        log.info("禁用任务分类: categoryId={}", categoryId);

        taskCategoryService.disableCategory(categoryId);
        return Result.success();
    }

    /**
     * 启用分类
     */
    @PutMapping("/{categoryId}/enable")
    @Operation(summary = "启用分类", description = "启用任务分类")
    public Result<Void> enableCategory(
            @Parameter(description = "分类ID", required = true)
            @PathVariable @NotNull Long categoryId) {
        
        log.info("启用任务分类: categoryId={}", categoryId);

        taskCategoryService.enableCategory(categoryId);
        return Result.success();
    }
}
