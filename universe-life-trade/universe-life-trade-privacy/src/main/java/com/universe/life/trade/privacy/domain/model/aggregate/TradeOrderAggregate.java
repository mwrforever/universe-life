package com.universe.life.trade.privacy.domain.model.aggregate;

import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.trade.privacy.domain.model.valueobject.Money;
import com.universe.life.trade.privacy.domain.event.OrderCreatedEvent;
import com.universe.life.trade.privacy.domain.event.OrderStatusChangedEvent;
import com.universe.life.trade.privacy.domain.event.OrderCompletedEvent;
import com.universe.life.trade.privacy.domain.model.valueobject.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 交易订单聚合根
 * <p>
 * 交易订单代表用户接单申请及其后续的执行过程。
 * 聚合根封装了订单的所有业务规则和状态变更逻辑，确保订单数据的一致性。
 * </p>
 * <p>
 * 订单生命周期：
 * <ol>
 *   <li>待审批(PENDING) - 用户申请接单后的初始状态</li>
 *   <li>进行中(PROGRESS) - 发布者同意接单后开始执行</li>
 *   <li>待确认(SUBMIT) - 接单者提交成果等待验收</li>
 *   <li>待收款(PAYMENT) - 发布者确认验收后等待付款</li>
 *   <li>已完成(COMPLETED) - 付款完成，订单结束</li>
 * </ol>
 * </p>
 * <p>
 * 业务规则：
 * <ul>
 *   <li>只有发布者可以审批接单申请</li>
 *   <li>只有接单者可以提交成果和放弃任务</li>
 *   <li>发布者和接单者都可以发起申诉</li>
 *   <li>状态转换必须遵循预定义的状态机规则</li>
 * </ul>
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Getter
public class TradeOrderAggregate {

    private OrderId id;
    private Long taskId;
    private Long publisherId;
    private Long acceptorId;
    private Money rewardAmount;
    private TradeOrderStatusEnum status;
    private SubmitResult submitResult;
    private RejectInfo rejectInfo;
    private OrderTimeline timeline;
    private LocalDateTime appliedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    /**
     * 领域事件列表
     */
    private final List<Object> domainEvents = new ArrayList<>();

    private TradeOrderAggregate() {
    }

    /**
     * 创建新订单（申请接单）
     */
    public static TradeOrderAggregate create(Long taskId, Long publisherId, Long acceptorId, Long rewardAmount) {
        TradeOrderAggregate order = new TradeOrderAggregate();
        order.taskId = taskId;
        order.publisherId = publisherId;
        order.acceptorId = acceptorId;
        order.rewardAmount = Money.ofCents(rewardAmount);
        order.status = TradeOrderStatusEnum.PENDING;
        order.timeline = OrderTimeline.empty().addEvent("APPLIED", "提交接单申请");
        order.appliedAt = LocalDateTime.now();
        order.createdAt = LocalDateTime.now();
        order.updatedAt = LocalDateTime.now();
        order.version = 0L;

        // 发布创建事件
        order.domainEvents.add(OrderCreatedEvent.builder()
                .taskId(taskId)
                .acceptorId(acceptorId)
                .createdAt(order.appliedAt)
                .build());
        return order;
    }

    /**
     * 从持久化数据重建
     */
    public static TradeOrderAggregate reconstitute(
            Long id, Long taskId, Long publisherId, Long acceptorId, Long rewardAmount,
            TradeOrderStatusEnum status, String submitContent, List<String> submitImages,
            String rejectReason, LocalDateTime rejectedAt, Long rejectedBy,
            LocalDateTime appliedAt, LocalDateTime approvedAt, LocalDateTime submittedAt,
            LocalDateTime completedAt, LocalDateTime createdAt, LocalDateTime updatedAt, Long version) {

        TradeOrderAggregate order = new TradeOrderAggregate();
        order.id = OrderId.of(id);
        order.taskId = taskId;
        order.publisherId = publisherId;
        order.acceptorId = acceptorId;
        order.rewardAmount = Money.ofCents(rewardAmount);
        order.status = status;
        order.submitResult = SubmitResult.reconstitute(submitContent, submitImages, submittedAt);
        order.rejectInfo = RejectInfo.reconstitute(rejectReason, rejectedAt, rejectedBy);
        order.timeline = OrderTimeline.empty(); // 时间线需要从事件表重建
        order.appliedAt = appliedAt;
        order.approvedAt = approvedAt;
        order.submittedAt = submittedAt;
        order.completedAt = completedAt;
        order.createdAt = createdAt;
        order.updatedAt = updatedAt;
        order.version = version;
        return order;
    }

    /**
     * 设置ID（创建后由仓储设置）
     */
    public void setId(Long id) {
        this.id = OrderId.of(id);
    }

    /**
     * 设置时间线（从事件表重建）
     */
    public void setTimeline(OrderTimeline timeline) {
        this.timeline = timeline;
    }

    // ==================== 状态变更方法 ====================

    /**
     * 同意接单 - 进入进行中状态
     */
    public void approve() {
        validateStatusTransition(TradeOrderStatusEnum.PROGRESS);
        TradeOrderStatusEnum oldStatus = this.status;
        this.status = TradeOrderStatusEnum.PROGRESS;
        this.approvedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.timeline = this.timeline.addEvent("APPROVED", "发布者同意接单");
        domainEvents.add(OrderStatusChangedEvent.builder()
                .orderId(getIdValue())
                .taskId(this.taskId)
                .oldStatus(oldStatus)
                .newStatus(this.status)
                .changedAt(LocalDateTime.now())
                .build());
    }

    /**
     * 拒绝接单
     */
    public void reject(String reason) {
        validateStatusTransition(TradeOrderStatusEnum.REJECTED);
        TradeOrderStatusEnum oldStatus = this.status;
        this.status = TradeOrderStatusEnum.REJECTED;
        this.rejectInfo = RejectInfo.of(reason, this.publisherId);
        this.updatedAt = LocalDateTime.now();
        this.timeline = this.timeline.addEvent("REJECTED", "发布者拒绝接单: " + reason);
        domainEvents.add(OrderStatusChangedEvent.builder()
                .orderId(getIdValue())
                .taskId(this.taskId)
                .oldStatus(oldStatus)
                .newStatus(this.status)
                .changedAt(LocalDateTime.now())
                .build());
    }

    /**
     * 提交成果 - 进入待确认状态
     */
    public void submit(String content, List<String> images) {
        validateStatusTransition(TradeOrderStatusEnum.SUBMIT);
        TradeOrderStatusEnum oldStatus = this.status;
        this.status = TradeOrderStatusEnum.SUBMIT;
        this.submitResult = SubmitResult.of(content, images);
        this.submittedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.timeline = this.timeline.addEvent("SUBMITTED", "提交任务成果");
        domainEvents.add(OrderStatusChangedEvent.builder()
                .orderId(getIdValue())
                .taskId(this.taskId)
                .oldStatus(oldStatus)
                .newStatus(this.status)
                .changedAt(LocalDateTime.now())
                .build());
    }

    /**
     * 确认验收 - 进入待收款状态
     */
    public void confirm() {
        validateStatusTransition(TradeOrderStatusEnum.PAYMENT);
        TradeOrderStatusEnum oldStatus = this.status;
        this.status = TradeOrderStatusEnum.PAYMENT;
        this.updatedAt = LocalDateTime.now();
        this.timeline = this.timeline.addEvent("CONFIRMED", "发布者确认验收");
        domainEvents.add(OrderStatusChangedEvent.builder()
                .orderId(getIdValue())
                .taskId(this.taskId)
                .oldStatus(oldStatus)
                .newStatus(this.status)
                .changedAt(LocalDateTime.now())
                .build());
    }

    /**
     * 打回修改 - 返回进行中状态
     */
    public void returnForRevision(String reason) {
        validateStatusTransition(TradeOrderStatusEnum.PROGRESS);
        TradeOrderStatusEnum oldStatus = this.status;
        this.status = TradeOrderStatusEnum.PROGRESS;
        this.submitResult = null;
        this.submittedAt = null;
        this.updatedAt = LocalDateTime.now();
        this.timeline = this.timeline.addEvent("RETURNED", "打回修改: " + reason);
        domainEvents.add(OrderStatusChangedEvent.builder()
                .orderId(getIdValue())
                .taskId(this.taskId)
                .oldStatus(oldStatus)
                .newStatus(this.status)
                .changedAt(LocalDateTime.now())
                .build());
    }

    /**
     * 发起申诉 - 进入争议中状态
     */
    public void dispute() {
        validateStatusTransition(TradeOrderStatusEnum.DISPUTE);
        TradeOrderStatusEnum oldStatus = this.status;
        this.status = TradeOrderStatusEnum.DISPUTE;
        this.updatedAt = LocalDateTime.now();
        this.timeline = this.timeline.addEvent("DISPUTED", "发起申诉");
        domainEvents.add(OrderStatusChangedEvent.builder()
                .orderId(getIdValue())
                .taskId(this.taskId)
                .oldStatus(oldStatus)
                .newStatus(this.status)
                .changedAt(LocalDateTime.now())
                .build());
    }

    /**
     * 完成订单
     */
    public void complete() {
        validateStatusTransition(TradeOrderStatusEnum.COMPLETED);
        TradeOrderStatusEnum oldStatus = this.status;
        this.status = TradeOrderStatusEnum.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.timeline = this.timeline.addEvent("COMPLETED", "订单已完成");
        domainEvents.add(OrderStatusChangedEvent.builder()
                .orderId(getIdValue())
                .taskId(this.taskId)
                .oldStatus(oldStatus)
                .newStatus(this.status)
                .changedAt(LocalDateTime.now())
                .build());
        domainEvents.add(OrderCompletedEvent.builder()
                .orderId(getIdValue())
                .taskId(this.taskId)
                .acceptorId(this.acceptorId)
                .completedAt(this.completedAt)
                .build());
    }

    /**
     * 进入待评价状态
     */
    public void enterRating() {
        validateStatusTransition(TradeOrderStatusEnum.RATE);
        TradeOrderStatusEnum oldStatus = this.status;
        this.status = TradeOrderStatusEnum.RATE;
        this.updatedAt = LocalDateTime.now();
        this.timeline = this.timeline.addEvent("RATING", "等待评价");
        domainEvents.add(OrderStatusChangedEvent.builder()
                .orderId(getIdValue())
                .taskId(this.taskId)
                .oldStatus(oldStatus)
                .newStatus(this.status)
                .changedAt(LocalDateTime.now())
                .build());
    }

    /**
     * 放弃任务
     */
    public void abandon() {
        validateStatusTransition(TradeOrderStatusEnum.ABANDONED);
        TradeOrderStatusEnum oldStatus = this.status;
        this.status = TradeOrderStatusEnum.ABANDONED;
        this.updatedAt = LocalDateTime.now();
        this.timeline = this.timeline.addEvent("ABANDONED", "接单者放弃任务");
        domainEvents.add(OrderStatusChangedEvent.builder()
                .orderId(getIdValue())
                .taskId(this.taskId)
                .oldStatus(oldStatus)
                .newStatus(this.status)
                .changedAt(LocalDateTime.now())
                .build());
    }

    /**
     * 申诉处理完成 - 进入待收款
     */
    public void resolveDisputeToPayment() {
        validateStatusTransition(TradeOrderStatusEnum.PAYMENT);
        TradeOrderStatusEnum oldStatus = this.status;
        this.status = TradeOrderStatusEnum.PAYMENT;
        this.updatedAt = LocalDateTime.now();
        this.timeline = this.timeline.addEvent("DISPUTE_RESOLVED", "申诉处理完成，进入待收款");
        domainEvents.add(OrderStatusChangedEvent.builder()
                .orderId(getIdValue())
                .taskId(this.taskId)
                .oldStatus(oldStatus)
                .newStatus(this.status)
                .changedAt(LocalDateTime.now())
                .build());
    }

    /**
     * 申诉驳回 - 返回进行中
     */
    public void resolveDisputeToProgress() {
        validateStatusTransition(TradeOrderStatusEnum.PROGRESS);
        TradeOrderStatusEnum oldStatus = this.status;
        this.status = TradeOrderStatusEnum.PROGRESS;
        this.updatedAt = LocalDateTime.now();
        this.timeline = this.timeline.addEvent("DISPUTE_REJECTED", "申诉驳回，继续执行任务");
        domainEvents.add(OrderStatusChangedEvent.builder()
                .orderId(getIdValue())
                .taskId(this.taskId)
                .oldStatus(oldStatus)
                .newStatus(this.status)
                .changedAt(LocalDateTime.now())
                .build());
    }

    // ==================== 业务方法 ====================

    /**
     * 是否可以操作（检查用户权限）
     */
    public boolean canOperate(Long userId) {
        return isPublisher(userId) || isAcceptor(userId);
    }

    /**
     * 是否是发布者
     */
    public boolean isPublisher(Long userId) {
        return this.publisherId.equals(userId);
    }

    /**
     * 是否是接单者
     */
    public boolean isAcceptor(Long userId) {
        return this.acceptorId.equals(userId);
    }

    /**
     * 获取可用操作列表
     */
    public List<String> getAvailableActions(Long userId) {
        List<String> actions = new ArrayList<>();

        if (isPublisher(userId)) {
            // 发布者可用操作
            if (status.canApprove()) {
                actions.add("APPROVE");
                actions.add("REJECT");
            }
            if (status.canConfirm()) {
                actions.add("CONFIRM");
                actions.add("RETURN");
            }
        }

        if (isAcceptor(userId)) {
            // 接单者可用操作
            if (status.canSubmit()) {
                actions.add("SUBMIT");
            }
            if (status.canAbandon()) {
                actions.add("ABANDON");
            }
            if (status.canDispute()) {
                actions.add("APPEAL");
            }
        }

        return actions;
    }

    // ==================== 辅助方法 ====================

    private void validateStatusTransition(TradeOrderStatusEnum targetStatus) {
        if (!status.canTransitionTo(targetStatus)) {
            throw new BusinessException.OperationNotAllowedException(
                    String.format("非法的状态转换: %s -> %s", status.getDesc(), targetStatus.getDesc()));
        }
    }

    /**
     * 获取并清空领域事件
     */
    public List<Object> pullDomainEvents() {
        List<Object> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    /**
     * 获取ID值
     */
    public Long getIdValue() {
        return id != null ? id.getValue() : null;
    }

    /**
     * 获取悬赏金额（分）
     */
    public Long getRewardAmountCents() {
        return rewardAmount.getCents();
    }

    /**
     * 获取悬赏金额（元）
     */
    public String getRewardAmountYuan() {
        return rewardAmount.toYuanString();
    }

    /**
     * 是否有提交成果
     */
    public boolean hasSubmitResult() {
        return submitResult != null;
    }

    /**
     * 是否有拒绝信息
     */
    public boolean hasRejectInfo() {
        return rejectInfo != null;
    }
}
