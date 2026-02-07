package com.universe.life.aftercare.interfaces.controller;

import com.universe.life.aftercare.application.service.AftercareAppealService;
import com.universe.life.aftercare.interfaces.dto.request.HandleAppealRequest;
import com.universe.life.aftercare.interfaces.vo.AftercareAppealVO;
import com.universe.life.auth.common.domain.Result;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.common.domain.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/appeals")
@RequiredArgsConstructor
@Tag(name = "申诉管理（管理端）", description = "申诉审核相关接口")
public class AdminAppealController {

    private final AftercareAppealService appealService;

    @GetMapping
    @Operation(summary = "待处理申诉列表", description = "分页查询待处理的申诉列表")
    public Result<PageResult<AftercareAppealVO>> getPendingAppeals(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("查询待处理申诉列表: pageNum={}, pageSize={}", pageNum, pageSize);
        return Result.success(appealService.pagePendingAppeals(pageNum, pageSize));
    }

    @PutMapping("/{appealId}/handle")
    @Operation(summary = "处理申诉", description = "管理员处理申诉")
    public Result<Void> handleAppeal(
            @Parameter(description = "申诉ID", required = true) @PathVariable @NotNull Long appealId,
            @Valid @RequestBody HandleAppealRequest request) {

        Long handlerId = SecurityUtil.getUserId();
        log.info("处理申诉: appealId={}, handlerId={}, result={}", appealId, handlerId, request.getResult());

        appealService.handleAppeal(appealId, handlerId, request.getResult(), request.getHandleRemark(),
                request.getPayableAmountCents(), request.getPaidAmountCents());
        return Result.success();
    }
}
