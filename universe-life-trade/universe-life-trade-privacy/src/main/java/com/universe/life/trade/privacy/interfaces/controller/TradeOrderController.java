package com.universe.life.trade.privacy.interfaces.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.common.domain.PageResult;
import com.universe.life.trade.privacy.application.command.*;
import com.universe.life.trade.privacy.application.query.MyOrdersQuery;
import com.universe.life.trade.privacy.application.query.OrderDetailQuery;
import com.universe.life.trade.privacy.application.query.TaskOrdersQuery;
import com.universe.life.trade.privacy.application.service.TradeOrderApplicationService;
import com.universe.life.trade.privacy.interfaces.dto.request.*;
import com.universe.life.trade.privacy.interfaces.vo.TradeOrderFullDetailVO;
import com.universe.life.trade.privacy.interfaces.vo.TradeOrderSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 交易订单控制器
 * <p>
 * 提供交易订单相关的HTTP接口，包括：
 * <ul>
 *   <li>申请接单 - 用户申请接受某个任务</li>
 *   <li>审批接单 - 发布者同意或拒绝接单申请</li>
 *   <li>提交成果 - 接单者提交任务完成成果</li>
 *   <li>确认验收 - 发布者确认成果验收通过</li>
 *   <li>放弃任务 - 接单者主动放弃任务</li>
 *   <li>发起申诉 - 对订单结果发起申诉</li>
 *   <li>订单查询 - 订单详情、列表查询</li>
 * </ul>
 * </p>
 * <p>
 * 本控制器遵循DDD架构，只负责：
 * <ul>
 *   <li>接收HTTP请求并验证参数</li>
 *   <li>将Request转换为Command/Query</li>
 *   <li>调用应用服务处理业务</li>
 *   <li>返回统一格式的响应</li>
 * </ul>
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Tag(name = "交易订单管理", description = "接单申请、订单管理、成果提交等相关接口")
@RestController
@RequestMapping("/api/v1/trade/orders")
@RequiredArgsConstructor
@Validated
public class TradeOrderController {

    /** 交易订单应用服务 */
    private final TradeOrderApplicationService orderApplicationService;

    /**
     * 申请接单
     * <p>
     * 用户申请接受指定任务，创建订单后状态为"待审批"，
     * 等待任务发布者审批。同一用户不能重复申请同一任务。
     * </p>
     *
     * @param request 申请接单请求参数
     * @return 新创建的订单ID
     */
    @PostMapping
    @Operation(summary = "申请接单", description = "申请接受指定任务")
    public Result<Long> applyOrder(@RequestBody @Validated TradeOrderApplyRequest request) {
        // 1. 构建申请接单命令
        ApplyOrderCommand command = ApplyOrderCommand.builder()
                .taskId(request.getTaskId())
                .acceptorId(SecurityUtil.getUserId())
                .build();
        
        // 2. 调用应用服务申请接单
        Long orderId = orderApplicationService.applyOrder(command);
        
        return Result.success(orderId);
    }

    /**
     * 同意接单
     * <p>
     * 发布者同意接单申请，订单状态变为"进行中"。
     * 只有任务发布者可以操作。
     * </p>
     *
     * @param orderId 订单ID
     * @return 操作结果
     */
    @PutMapping("/{orderId}/approve")
    @Operation(summary = "同意接单", description = "发布者同意接单申请")
    public Result<Void> approveOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable Long orderId) {
        // 1. 构建同意接单命令
        ApproveOrderCommand command = ApproveOrderCommand.builder()
                .orderId(orderId)
                .operatorId(SecurityUtil.getUserId())
                .build();
        
        // 2. 调用应用服务同意接单
        orderApplicationService.approveOrder(command);
        
        return Result.success();
    }

    /**
     * 拒绝接单
     * <p>
     * 发布者拒绝接单申请，需要提供拒绝原因。
     * 订单状态变为"已拒绝"。只有任务发布者可以操作。
     * </p>
     *
     * @param orderId 订单ID
     * @param request 拒绝接单请求参数
     * @return 操作结果
     */
    @PutMapping("/{orderId}/reject")
    @Operation(summary = "拒绝接单", description = "发布者拒绝接单申请")
    public Result<Void> rejectOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable Long orderId,
            @RequestBody @Validated TradeRejectRequest request) {
        // 1. 构建拒绝接单命令
        RejectOrderCommand command = RejectOrderCommand.builder()
                .orderId(orderId)
                .operatorId(SecurityUtil.getUserId())
                .reason(request.getReason())
                .build();
        
        // 2. 调用应用服务拒绝接单
        orderApplicationService.rejectOrder(command);
        
        return Result.success();
    }

    /**
     * 提交成果
     * <p>
     * 接单者提交任务完成成果，包括文字说明和图片证明。
     * 提交后订单状态变为"待确认"，等待发布者验收。
     * 只有接单者可以操作，且订单状态必须为"进行中"。
     * </p>
     *
     * @param orderId 订单ID
     * @param request 提交成果请求参数
     * @return 操作结果
     */
    @PutMapping("/{orderId}/submit")
    @Operation(summary = "提交成果", description = "接单者提交任务成果")
    public Result<Void> submitResult(
            @Parameter(description = "订单ID", required = true) @PathVariable Long orderId,
            @RequestBody @Validated TradeSubmitRequest request) {
        // 1. 构建提交成果命令
        SubmitResultCommand command = SubmitResultCommand.builder()
                .orderId(orderId)
                .operatorId(SecurityUtil.getUserId())
                .content(request.getContent())
                .images(request.getImages())
                .build();
        
        // 2. 调用应用服务提交成果
        orderApplicationService.submitResult(command);
        
        return Result.success();
    }

    /**
     * 确认验收
     * <p>
     * 发布者确认成果验收通过，订单进入"待收款"状态。
     * 只有任务发布者可以操作，且订单状态必须为"待确认"。
     * </p>
     *
     * @param orderId 订单ID
     * @param request 确认验收请求参数（可选）
     * @return 操作结果
     */
    @PutMapping("/{orderId}/confirm")
    @Operation(summary = "确认验收", description = "发布者确认验收成果")
    public Result<Void> confirmResult(
            @Parameter(description = "订单ID", required = true) @PathVariable Long orderId,
            @RequestBody(required = false) TradeConfirmRequest request) {
        // 1. 构建确认验收命令
        ConfirmResultCommand command = ConfirmResultCommand.builder()
                .orderId(orderId)
                .operatorId(SecurityUtil.getUserId())
                .build();
        
        // 2. 调用应用服务确认验收
        orderApplicationService.confirmResult(command);
        
        return Result.success();
    }

    /**
     * 放弃任务
     * <p>
     * 接单者主动放弃任务，订单状态变为"已放弃"。
     * 放弃任务可能会影响接单者的信用评分。
     * 只有接单者可以操作，且订单状态必须为"进行中"。
     * </p>
     *
     * @param orderId 订单ID
     * @return 操作结果
     */
    @PutMapping("/{orderId}/abandon")
    @Operation(summary = "放弃任务", description = "接单者放弃任务")
    public Result<Void> abandonOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable Long orderId) {
        // 1. 构建放弃任务命令
        AbandonOrderCommand command = AbandonOrderCommand.builder()
                .orderId(orderId)
                .operatorId(SecurityUtil.getUserId())
                .build();
        
        // 2. 调用应用服务放弃任务
        orderApplicationService.abandonOrder(command);
        
        return Result.success();
    }

    /**
     * 发起申诉
     * <p>
     * 对订单结果发起申诉，订单状态变为"争议中"。
     * 发布者和接单者都可以发起申诉。
     * 同一订单只能有一个待处理的申诉。
     * </p>
     *
     * @param orderId 订单ID
     * @param request 发起申诉请求参数
     * @return 操作结果
     */
    @PostMapping("/{orderId}/appeal")
    @Operation(summary = "发起申诉", description = "对订单发起申诉")
    public Result<Void> initiateAppeal(
            @Parameter(description = "订单ID", required = true) @PathVariable Long orderId,
            @RequestBody @Validated TradeAppealRequest request) {
        // 1. 构建发起申诉命令
        InitiateAppealCommand command = InitiateAppealCommand.builder()
                .orderId(orderId)
                .operatorId(SecurityUtil.getUserId())
                .appealType(request.getAppealType())
                .reason(request.getReason())
                .evidenceImages(request.getEvidenceImages())
                .build();
        
        // 2. 调用应用服务发起申诉
        orderApplicationService.initiateAppeal(command);
        
        return Result.success();
    }

    /**
     * 获取订单详情
     * <p>
     * 获取订单的完整信息，包括：
     * <ul>
     *   <li>订单基本信息</li>
     *   <li>任务信息</li>
     *   <li>发布者和接单者信息</li>
     *   <li>提交成果信息</li>
     *   <li>申诉信息</li>
     *   <li>时间线</li>
     *   <li>当前用户的可用操作</li>
     * </ul>
     * 只有发布者和接单者可以查看订单详情。
     * </p>
     *
     * @param orderId 订单ID
     * @return 订单完整详情
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "订单详情", description = "获取订单详细信息")
    public Result<TradeOrderFullDetailVO> getOrderDetail(
            @Parameter(description = "订单ID", required = true) @PathVariable Long orderId) {
        // 1. 构建订单详情查询
        OrderDetailQuery query = OrderDetailQuery.builder()
                .orderId(orderId)
                .currentUserId(SecurityUtil.getUserId())
                .build();
        
        // 2. 调用应用服务查询订单详情
        TradeOrderFullDetailVO detail = orderApplicationService.getOrderDetail(query);
        
        return Result.success(detail);
    }

    /**
     * 我的接单列表
     * <p>
     * 分页查询当前用户作为接单者的订单列表，支持按状态筛选。
     * </p>
     *
     * @param request 我的接单查询参数
     * @return 分页结果
     */
    @GetMapping("/my")
    @Operation(summary = "我的接单列表", description = "查询当前用户的接单列表")
    public Result<PageResult<TradeOrderSummaryVO>> pageMyOrders(@Validated TradeOrderQueryRequest request) {
        // 1. 构建我的接单查询
        MyOrdersQuery query = MyOrdersQuery.builder()
                .acceptorId(SecurityUtil.getUserId())
                .status(request.getStatus())
                .pageNum(request.getPageNum())
                .pageSize(request.getPageSize())
                .build();
        
        // 2. 调用应用服务查询订单列表
        PageResult<TradeOrderSummaryVO> page = orderApplicationService.pageMyOrders(query);
        
        return Result.success(page);
    }

    /**
     * 任务的接单列表
     * <p>
     * 分页查询指定任务的所有接单申请，供发布者查看和审批。
     * 支持按状态筛选。
     * </p>
     *
     * @param taskId 任务ID
     * @param request 任务接单查询参数
     * @return 分页结果
     */
    @GetMapping("/task/{taskId}")
    @Operation(summary = "任务的接单列表", description = "查询指定任务的接单列表（发布者使用）")
    public Result<PageResult<TradeOrderSummaryVO>> pageTaskOrders(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId,
            @Validated TradeOrderQueryRequest request) {
        // 1. 构建任务接单查询
        TaskOrdersQuery query = TaskOrdersQuery.builder()
                .taskId(taskId)
                .currentUserId(SecurityUtil.getUserId())
                .status(request.getStatus())
                .pageNum(request.getPageNum())
                .pageSize(request.getPageSize())
                .build();
        
        // 2. 调用应用服务查询订单列表
        PageResult<TradeOrderSummaryVO> page = orderApplicationService.pageTaskOrders(query);
        
        return Result.success(page);
    }
}
