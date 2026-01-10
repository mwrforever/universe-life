package com.universe.life.trade.privacy.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.common.domain.PageResult;
import com.universe.life.trade.privacy.domain.dao.query.TradeOrderQuery;
import com.universe.life.trade.privacy.domain.dto.request.*;
import com.universe.life.trade.privacy.domain.vo.TradeOrderVO;
import com.universe.life.trade.privacy.service.ITradeOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 交易订单控制器
 *
 * @author universe-life
 */
@Tag(name = "交易订单管理", description = "交易订单相关接口")
@RestController
@RequestMapping("/trade/order")
@RequiredArgsConstructor
public class TradeOrderController {

    private final ITradeOrderService tradeOrderService;

    @Operation(summary = "申请接单", description = "创建交易订单，申请接受某个需求")
    @PostMapping("/apply")
    public Result<Long> applyOrder(@Valid @RequestBody TradeOrderCreateRequest request) {
        Long userId = SecurityUtil.getUserId();
        Long orderId = tradeOrderService.createOrder(request.getTaskId(), userId);
        return Result.success(orderId);
    }

    @Operation(summary = "同意接单申请", description = "发布者同意接单者的申请")
    @PutMapping("/{orderId}/approve")
    public Result<Void> approveOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Long userId = SecurityUtil.getUserId();
        tradeOrderService.approveOrder(orderId, userId);
        return Result.success();
    }

    @Operation(summary = "拒绝接单申请", description = "发布者拒绝接单者的申请")
    @PutMapping("/{orderId}/reject")
    public Result<Void> rejectOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            @Valid @RequestBody TradeRejectRequest request) {
        Long userId = SecurityUtil.getUserId();
        tradeOrderService.rejectOrder(orderId, request, userId);
        return Result.success();
    }

    @Operation(summary = "提交任务成果", description = "接单者提交任务完成成果")
    @PutMapping("/{orderId}/submit")
    public Result<Void> submitResult(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            @Valid @RequestBody TradeSubmitRequest request) {
        Long userId = SecurityUtil.getUserId();
        tradeOrderService.submitResult(orderId, request, userId);
        return Result.success();
    }

    @Operation(summary = "确认验收", description = "发布者确认验收接单者提交的成果")
    @PutMapping("/{orderId}/confirm")
    public Result<Void> confirmResult(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            @Valid @RequestBody TradeConfirmRequest request) {
        Long userId = SecurityUtil.getUserId();
        tradeOrderService.confirmResult(orderId, request, userId);
        return Result.success();
    }

    @Operation(summary = "放弃任务", description = "接单者放弃已接受的任务")
    @PutMapping("/{orderId}/abandon")
    public Result<Void> abandonOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Long userId = SecurityUtil.getUserId();
        tradeOrderService.abandonOrder(orderId, userId);
        return Result.success();
    }

    @Operation(summary = "发起申诉", description = "对交易发起申诉")
    @PostMapping("/{orderId}/appeal")
    public Result<Long> initiateAppeal(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            @Valid @RequestBody TradeAppealRequest request) {
        Long userId = SecurityUtil.getUserId();
        Long appealId = tradeOrderService.initiateAppeal(orderId, request, userId);
        return Result.success(appealId);
    }

    @Operation(summary = "我的接单列表", description = "分页查询当前用户的接单列表")
    @GetMapping("/my")
    public Result<PageResult<TradeOrderVO>> pageMyOrders(@Valid TradeOrderQuery query) {
        Long userId = SecurityUtil.getUserId();
        PageResult<TradeOrderVO> result = tradeOrderService.pageMyOrders(query, userId);
        return Result.success(result);
    }

    @Operation(summary = "需求接单列表", description = "发布者查看某需求的接单列表")
    @GetMapping("/task/{taskId}")
    public Result<PageResult<TradeOrderVO>> pageTaskOrders(
            @Parameter(description = "需求ID") @PathVariable Long taskId,
            @Valid TradeOrderQuery query) {
        Long userId = SecurityUtil.getUserId();
        PageResult<TradeOrderVO> result = tradeOrderService.pageTaskOrders(taskId, query, userId);
        return Result.success(result);
    }

    @Operation(summary = "订单详情", description = "获取交易订单详情")
    @GetMapping("/{orderId}")
    public Result<TradeOrderVO> getOrderDetail(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Long userId = SecurityUtil.getUserId();
        TradeOrderVO result = tradeOrderService.getOrderDetail(orderId, userId);
        return Result.success(result);
    }
}
