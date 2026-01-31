package com.universe.life.trade.privacy.application.assembler;

import com.universe.life.trade.privacy.application.command.ApplyOrderCommand;
import com.universe.life.trade.privacy.application.command.SubmitResultCommand;
import com.universe.life.trade.privacy.domain.model.aggregate.TradeOrderAggregate;
import com.universe.life.trade.privacy.domain.model.valueobject.OrderTimeline;
import com.universe.life.trade.privacy.interfaces.dto.request.TradeOrderApplyRequest;
import com.universe.life.trade.privacy.interfaces.dto.request.TradeSubmitRequest;
import com.universe.life.trade.privacy.interfaces.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 交易订单对象转换器
 * <p>
 * 使用MapStruct在编译时生成转换代码，负责以下转换：
 * <ul>
 *   <li>Request → Command：接口层请求转应用层命令</li>
 *   <li>Aggregate → VO：领域聚合根转接口层视图对象</li>
 * </ul>
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TradeOrderAssembler {

    // ==================== Request → Command 转换 ====================

    /**
     * 申请接单请求转命令
     * <p>
     * 将接口层的申请接单请求转换为应用层命令对象。
     * acceptorId需要在Controller层从当前用户上下文获取后设置。
     * </p>
     *
     * @param request    申请接单请求
     * @param acceptorId 接单者ID（从当前用户上下文获取）
     * @return 申请接单命令
     */
    @Mapping(target = "taskId", source = "request.taskId")
    @Mapping(target = "acceptorId", source = "acceptorId")
    @Mapping(target = "applyNote", source = "request.remark")
    ApplyOrderCommand toApplyCommand(TradeOrderApplyRequest request, Long acceptorId);

    /**
     * 提交成果请求转命令
     * <p>
     * 将接口层的提交成果请求转换为应用层命令对象。
     * orderId和operatorId需要在Controller层设置。
     * </p>
     *
     * @param request    提交成果请求
     * @param orderId    订单ID
     * @param operatorId 操作者ID（接单者）
     * @return 提交成果命令
     */
    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "operatorId", source = "operatorId")
    @Mapping(target = "content", source = "request.content")
    @Mapping(target = "images", source = "request.images")
    SubmitResultCommand toSubmitCommand(TradeSubmitRequest request, Long orderId, Long operatorId);

    // ==================== Aggregate → VO 转换 ====================

    /**
     * 转换为订单摘要VO
     * <p>
     * 将订单聚合根转换为摘要视图对象，用于列表展示。
     * 需要额外传入任务和发布者信息。
     * </p>
     *
     * @param order             订单聚合根
     * @param taskTitle         任务标题
     * @param taskDeadline      任务截止时间
     * @param publisherNickname 发布者昵称
     * @param publisherAvatar   发布者头像
     * @return 订单摘要VO
     */
    default TradeOrderSummaryVO toSummaryVO(TradeOrderAggregate order, String taskTitle,
                                            LocalDateTime taskDeadline,
                                            String publisherNickname, String publisherAvatar) {
        if (order == null) {
            return null;
        }
        TradeOrderSummaryVO vo = new TradeOrderSummaryVO();
        vo.setOrderId(order.getIdValue());
        vo.setTaskId(order.getTaskId());
        vo.setTaskTitle(taskTitle);
        vo.setRewardAmount(order.getRewardAmountCents());
        vo.setRewardAmountYuan(order.getRewardAmountYuan());
        vo.setStatus(order.getStatus().getCode());
        vo.setStatusText(order.getStatus().getDesc());
        vo.setAppliedAt(order.getAppliedAt());
        vo.setTaskDeadline(taskDeadline);
        vo.setDeadlineText(formatDeadlineText(taskDeadline));
        vo.setPublisherNickname(publisherNickname);
        vo.setPublisherAvatar(publisherAvatar);
        return vo;
    }

    /**
     * 转换为订单完整详情VO
     * <p>
     * 将订单聚合根转换为完整详情视图对象，包含所有订单信息。
     * 需要额外传入任务、用户、申诉等关联信息。
     * </p>
     *
     * @param order           订单聚合根
     * @param taskTitle       任务标题
     * @param taskDescription 任务描述
     * @param taskDeadline    任务截止时间
     * @param publisher       发布者信息
     * @param acceptor        接单者信息
     * @param appealSummary   申诉摘要
     * @param currentUserId   当前用户ID（用于判断角色和可用操作）
     * @return 订单完整详情VO
     */
    default TradeOrderFullDetailVO toDetailVO(TradeOrderAggregate order,
                                              String taskTitle, String taskDescription,
                                              LocalDateTime taskDeadline,
                                              UserInfoVO publisher, UserInfoVO acceptor,
                                              AppealSummaryVO appealSummary,
                                              Long currentUserId) {
        if (order == null) {
            return null;
        }
        TradeOrderFullDetailVO vo = new TradeOrderFullDetailVO();
        vo.setOrderId(order.getIdValue());
        vo.setTaskId(order.getTaskId());
        vo.setTaskTitle(taskTitle);
        vo.setTaskDescription(taskDescription);
        vo.setRewardAmount(order.getRewardAmountCents());
        vo.setRewardAmountYuan(order.getRewardAmountYuan());
        vo.setStatus(order.getStatus().getCode());
        vo.setStatusText(order.getStatus().getDesc());
        vo.setAppliedAt(order.getAppliedAt());
        vo.setApprovedAt(order.getApprovedAt());
        vo.setSubmittedAt(order.getSubmittedAt());
        vo.setCompletedAt(order.getCompletedAt());
        vo.setTaskDeadline(taskDeadline);
        vo.setPublisher(publisher);
        vo.setAcceptor(acceptor);

        // 提交成果
        if (order.hasSubmitResult()) {
            vo.setSubmitResult(toSubmitResultVO(order));
        }

        // 拒绝信息
        if (order.hasRejectInfo()) {
            vo.setRejectInfo(toRejectInfoVO(order));
        }

        // 申诉摘要
        vo.setAppealSummary(appealSummary);

        // 时间线
        vo.setTimeline(toTimelineVOList(order.getTimeline()));

        // 角色判断
        vo.setIsPublisher(order.isPublisher(currentUserId));
        vo.setIsAcceptor(order.isAcceptor(currentUserId));

        // 可用操作
        vo.setAvailableActions(order.getAvailableActions(currentUserId));

        return vo;
    }

    /**
     * 转换为提交成果VO
     *
     * @param order 订单聚合根
     * @return 提交成果VO
     */
    default SubmitResultVO toSubmitResultVO(TradeOrderAggregate order) {
        if (order == null || !order.hasSubmitResult()) {
            return null;
        }
        SubmitResultVO vo = new SubmitResultVO();
        vo.setContent(order.getSubmitResult().getContent());
        vo.setImages(order.getSubmitResult().getImages());
        vo.setSubmittedAt(order.getSubmitResult().getSubmittedAt());
        return vo;
    }

    /**
     * 转换为拒绝信息VO
     *
     * @param order 订单聚合根
     * @return 拒绝信息VO
     */
    default RejectInfoVO toRejectInfoVO(TradeOrderAggregate order) {
        if (order == null || !order.hasRejectInfo()) {
            return null;
        }
        RejectInfoVO vo = new RejectInfoVO();
        vo.setReason(order.getRejectInfo().getReason());
        vo.setRejectedAt(order.getRejectInfo().getRejectedAt());
        vo.setRejectedBy(order.getRejectInfo().getRejectedBy());
        return vo;
    }

    /**
     * 转换为时间线VO列表
     *
     * @param timeline 订单时间线
     * @return 时间线VO列表
     */
    default List<OrderTimelineVO> toTimelineVOList(OrderTimeline timeline) {
        if (timeline == null || timeline.getEvents().isEmpty()) {
            return List.of();
        }
        return timeline.getEvents().stream()
                .map(event -> {
                    OrderTimelineVO vo = new OrderTimelineVO();
                    vo.setAction(event.getAction());
                    vo.setDescription(event.getDescription());
                    vo.setTime(event.getTime());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 格式化截止时间文本
     * <p>
     * 将截止时间转换为友好的剩余时间文本，如"剩余3天"、"剩余2小时"等。
     * </p>
     *
     * @param deadline 截止时间
     * @return 格式化后的文本
     */
    default String formatDeadlineText(LocalDateTime deadline) {
        if (deadline == null) {
            return "";
        }
        LocalDateTime now = LocalDateTime.now();
        if (deadline.isBefore(now)) {
            return "已过期";
        }
        long days = ChronoUnit.DAYS.between(now, deadline);
        if (days > 0) {
            return "剩余" + days + "天";
        }
        long hours = ChronoUnit.HOURS.between(now, deadline);
        if (hours > 0) {
            return "剩余" + hours + "小时";
        }
        long minutes = ChronoUnit.MINUTES.between(now, deadline);
        return "剩余" + minutes + "分钟";
    }
}
