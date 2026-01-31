package com.universe.life.user.privacy.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.common.domain.PageResult;
import com.universe.life.user.privacy.domain.dao.query.SysDepartmentListQuery;
import com.universe.life.user.privacy.domain.dto.request.SysDepartmentCreateRequest;
import com.universe.life.user.privacy.domain.dto.request.SysDepartmentStatusUpdateRequest;
import com.universe.life.user.privacy.domain.dto.request.SysDepartmentUpdateRequest;
import com.universe.life.user.privacy.domain.vo.SysDepartmentDetailVO;
import com.universe.life.user.privacy.domain.vo.SysDepartmentListVO;
import com.universe.life.user.privacy.domain.vo.SysDepartmentTreeVO;
import com.universe.life.user.privacy.service.ISysDepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Slf4j
@Tag(name = "部门管理", description = "部门CRUD相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/department")
@Validated
public class AdminSysDepartmentController {

    private final ISysDepartmentService departmentService;

    @PostMapping
    @Operation(summary = "创建部门", description = "创建新的部门")
    @PreAuthorize("@pm.match('sys:admin:department:add')")
    public Result<SysDepartmentDetailVO> createDepartment(@Valid @RequestBody SysDepartmentCreateRequest request) {
        log.info("创建部门，部门编码：{}", request.getDeptCode());
        return Result.success(departmentService.createDepartment(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取部门详情", description = "根据ID获取部门详细信息")
    @PreAuthorize("@pm.match('sys:admin:department:read')")
    public Result<SysDepartmentDetailVO> getDepartmentById(
            @Parameter(description = "部门ID", required = true) @PathVariable Long id) {
        log.info("查询部门详情，部门ID：{}", id);
        return Result.success(departmentService.getDepartmentById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新部门", description = "更新部门信息")
    @PreAuthorize("@pm.match('sys:admin:department:update')")
    public Result<Void> updateDepartment(
            @Parameter(description = "部门ID", required = true) @PathVariable Long id,
            @Valid @RequestBody SysDepartmentUpdateRequest request) {
        log.info("更新部门，部门ID：{}", id);
        departmentService.updateDepartment(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门", description = "删除部门")
    @PreAuthorize("@pm.match('sys:admin:department:delete')")
    public Result<Void> deleteDepartment(
            @Parameter(description = "部门ID", required = true) @PathVariable Long id) {
        log.info("删除部门，部门ID：{}", id);
        departmentService.deleteDepartment(id);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询部门列表", description = "分页查询部门列表")
    @PreAuthorize("@pm.match('sys:admin:department:list:read')")
    public Result<PageResult<SysDepartmentListVO>> pageDepartments(@Valid SysDepartmentListQuery query) {
        log.info("分页查询部门列表，查询条件：{}", query);
        return Result.success(departmentService.pageDepartments(query));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "修改部门状态", description = "修改部门启用/禁用状态")
    @PreAuthorize("@pm.match('sys:admin:department:status:update')")
    public Result<Void> updateDepartmentStatus(
            @Parameter(description = "部门ID", required = true) @PathVariable Long id,
            @Valid @RequestBody SysDepartmentStatusUpdateRequest request) {
        log.info("修改部门状态，部门ID：{}，状态：{}", id, request.getStatus());
        departmentService.updateDepartmentStatus(id, request);
        return Result.success();
    }

    @GetMapping("/tree")
    @Operation(summary = "获取部门树形结构", description = "获取所有启用部门的树形结构")
    @PreAuthorize("@pm.match('sys:admin:department:tree:read')")
    public Result<List<SysDepartmentTreeVO>> getDepartmentTree() {
        log.info("获取部门树形结构");
        return Result.success(departmentService.getDepartmentTree());
    }
}
