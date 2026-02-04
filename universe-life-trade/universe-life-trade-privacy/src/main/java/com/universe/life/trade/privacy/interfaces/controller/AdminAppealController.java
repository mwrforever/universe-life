package com.universe.life.trade.privacy.interfaces.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.common.domain.PageResult;
import com.universe.life.trade.privacy.application.service.TradeAppealService;
import com.universe.life.trade.privacy.infrastructure.enums.TradeAppealResult;
import com.universe.life.trade.privacy.interfaces.dto.request.HandleAppealRequest;
import com.universe.life.trade.privacy.interfaces.vo.TradeAppealVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 申诉管理端接口
 * <p>
 * 提供申诉管理相关的HTTP接口，供平台管理员使用：
 * <ul>
 *   <li>申诉列表查询 - 查看待处理的申诉</li>
 *   <li>处理申诉 - 审核申诉并给出处理结果</li>
 * </ul>
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/appeals")
@RequiredArgsConstructor
@Tag(name = "申诉管理（管理端）", description = "申诉审核相关接口")
public class AdminAppealController {

    /** 申诉应用服务 */
    private final TradeAppealService appealService;

    /**
     * 待处理申诉列表
     * <p>
     * 分页查询所有待处理的申诉，供管理员查看和处理。
     * </p>
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    @GetMapping
    @Operation(summary = "待处理申诉列表", description = "分页查询待处理的申诉列表")
    public Result<PageResult<TradeAppealVO>> getPendingAppeals(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        log.info("查询待处理申诉列表: pageNum={}, pageSize={}", pageNum, pageSize);

        PageResult<TradeAppealVO> page = appealService.pagePendingAppeals(pageNum, pageSize);
        
        return Result.success(page);
    }

    /**
     * 处理申诉
     * <p>
     * 管理员处理申诉，给出处理结果和备注。
     * 处理结果包括：支持申诉方、驳回申诉。
     * </p>
     *
     * @param appealId 申诉ID
     * @param request  处理申诉请求参数
     * @return 操作结果
     */
    @PutMapping("/{appealId}/handle")
    @Operation(summary = "处理申诉", description = "管理员处理申诉")
    public Result<Void> handleAppeal(
            @Parameter(description = "申诉ID", required = true)
            @PathVariable @NotNull Long appealId,
            @Valid @RequestBody HandleAppealRequest request) {
        
        Long handlerId = SecurityUtil.getUserId();
        
        log.info("处理申诉: appealId={}, handlerId={}, result={}", 
                appealId, handlerId, request.getResult());

        // 转换处理结果为枚举
        TradeAppealResult result = TradeAppealResult.of(request.getResult());
        
        appealService.handleAppeal(appealId, handlerId, result, request.getHandleRemark());
        
        return Result.success();
    }
}
