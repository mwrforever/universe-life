package com.universe.life.trade.privacy.service.impl;

import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.common.domain.PageResult;
import com.universe.life.task.api.client.TaskClient;
import com.universe.life.task.model.dto.TaskInfoDTO;
import com.universe.life.task.model.enums.TaskStatus;
import com.universe.life.trade.privacy.domain.dao.query.TradeOrderQuery;
import com.universe.life.trade.privacy.domain.dto.TradeOrderDTO;
import com.universe.life.trade.privacy.domain.dto.request.*;
import com.universe.life.trade.privacy.domain.po.TradeAppeal;
import com.universe.life.trade.privacy.domain.po.TradeOrder;
import com.universe.life.trade.privacy.domain.vo.TradeOrderVO;
import com.universe.life.trade.privacy.enums.TradeAppealStatus;
import com.universe.life.trade.privacy.enums.TradeAppealType;
import com.universe.life.trade.privacy.enums.TradeOrderStatus;
import com.universe.life.trade.privacy.mapper.TradeAppealMapper;
import com.universe.life.trade.privacy.mapper.TradeOrderMapper;
import com.universe.life.trade.privacy.mapstruct.JsonConvertMapstruct;
import com.universe.life.trade.privacy.mapstruct.TradeOrderMapstruct;
import com.universe.life.trade.privacy.service.ITradeOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易订单服务实现
 *
 * @author universe-life
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeOrderServiceImpl implements ITradeOrderService {

    private final TradeOrderMapper orderMapper;
    private final TradeAppealMapper appealMapper;
    private final TradeOrderMapstruct orderMapstruct;
    private final TaskClient taskClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Long taskId, Long acceptorId) {
        // 1. 获取任务信息
        TaskInfoDTO taskInfo = taskClient.getTaskInfo(taskId);
        if (taskInfo == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("需求"));
        }

        // 2. 校验任务状态（必须是招募中）
        if (taskInfo.getStatus() != TaskStatus.RECRUITING) {
            throw new BusinessException.OperationNotAllowedException("该需求当前不接受接单申请");
        }

        // 3. 校验不能接自己发布的需求
        if (taskInfo.getPublisherId().equals(acceptorId)) {
            throw new BusinessException.OperationNotAllowedException("不能接受自己发布的需求");
        }

        // 4. 校验接单人数是否已满
        if (taskInfo.getCurrentAcceptors() >= taskInfo.getMaxAcceptors()) {
            throw new BusinessException.OperationNotAllowedException("需求接单人数已满");
        }

        // 5. 校验是否已存在未完结的订单
        int existingCount = orderMapper.checkUserAccepted(taskId, acceptorId);
        if (existingCount > 0) {
            throw new BusinessException.DataAlreadyExistsException("您已对该需求提交过接单申请");
        }

        // 6. 创建交易订单
        TradeOrder order = new TradeOrder();
        order.setTaskId(taskId);
        order.setPublisherId(taskInfo.getPublisherId());
        order.setAcceptorId(acceptorId);
        order.setRewardAmount(taskInfo.getRewardAmount());
        order.setStatus(TradeOrderStatus.PENDING);
        order.setAppliedAt(LocalDateTime.now());
        orderMapper.insert(order);

        // 7. 更新任务状态为待审批
        taskClient.updateTaskStatus(taskId, TaskStatus.PENDING);

        log.info("创建交易订单成功: orderId={}, taskId={}, acceptorId={}", order.getId(), taskId, acceptorId);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveOrder(Long orderId, Long publisherId) {
        // 1. 查询订单
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("订单"));
        }

        // 2. 校验权限
        if (!order.getPublisherId().equals(publisherId)) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.ACCESS_DENIED);
        }

        // 3. 校验状态
        if (order.getStatus() != TradeOrderStatus.PENDING) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.STATUS_INVALID);
        }

        // 4. 更新订单状态
        order.setStatus(TradeOrderStatus.PROGRESS);
        order.setApprovedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 5. 更新任务状态和接单人数
        taskClient.updateTaskStatus(order.getTaskId(), TaskStatus.PROGRESS);
        taskClient.increaseAcceptors(order.getTaskId());

        log.info("同意接单申请: orderId={}, publisherId={}", orderId, publisherId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectOrder(Long orderId, TradeRejectRequest request, Long publisherId) {
        // 1. 查询订单
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("订单"));
        }

        // 2. 校验权限
        if (!order.getPublisherId().equals(publisherId)) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.ACCESS_DENIED);
        }

        // 3. 校验状态
        if (order.getStatus() != TradeOrderStatus.PENDING) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.STATUS_INVALID);
        }

        // 4. 更新订单状态
        order.setStatus(TradeOrderStatus.REJECTED);
        order.setRejectReason(request.getReason());
        orderMapper.updateById(order);

        // 5. 检查是否还有其他待审批订单，如果没有则恢复任务状态为招募中
        int pendingCount = orderMapper.countByTaskIdAndStatus(order.getTaskId(), TradeOrderStatus.PENDING.getCode());
        if (pendingCount == 0) {
            taskClient.updateTaskStatus(order.getTaskId(), TaskStatus.RECRUITING);
        }

        log.info("拒绝接单申请: orderId={}, publisherId={}, reason={}", orderId, publisherId, request.getReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitResult(Long orderId, TradeSubmitRequest request, Long acceptorId) {
        // 1. 查询订单
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("订单"));
        }

        // 2. 校验权限
        if (!order.getAcceptorId().equals(acceptorId)) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.ACCESS_DENIED);
        }

        // 3. 校验状态
        if (order.getStatus() != TradeOrderStatus.PROGRESS) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.STATUS_INVALID);
        }

        // 4. 更新订单
        order.setStatus(TradeOrderStatus.SUBMIT);
        order.setSubmitContent(request.getContent());
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            order.setSubmitImages(JsonConvertMapstruct.stringListToJson(request.getImages()));
        }
        order.setSubmittedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 5. 更新任务状态为待验收
        taskClient.updateTaskStatus(order.getTaskId(), TaskStatus.REVIEW);

        log.info("提交任务成果: orderId={}, acceptorId={}", orderId, acceptorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmResult(Long orderId, TradeConfirmRequest request, Long publisherId) {
        // 1. 查询订单
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("订单"));
        }

        // 2. 校验权限
        if (!order.getPublisherId().equals(publisherId)) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.ACCESS_DENIED);
        }

        // 3. 校验状态
        if (order.getStatus() != TradeOrderStatus.SUBMIT) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.STATUS_INVALID);
        }

        // 4. 根据确认结果更新状态
        if (request.getConfirmed()) {
            order.setStatus(TradeOrderStatus.PAYMENT);
            taskClient.updateTaskStatus(order.getTaskId(), TaskStatus.PAYMENT);
        } else {
            // 拒绝验收，进入争议状态
            order.setStatus(TradeOrderStatus.DISPUTE);
            taskClient.updateTaskStatus(order.getTaskId(), TaskStatus.DISPUTE);
        }
        orderMapper.updateById(order);

        log.info("确认验收: orderId={}, publisherId={}, confirmed={}", orderId, publisherId, request.getConfirmed());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void abandonOrder(Long orderId, Long acceptorId) {
        // 1. 查询订单
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("订单"));
        }

        // 2. 校验权限
        if (!order.getAcceptorId().equals(acceptorId)) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.ACCESS_DENIED);
        }

        // 3. 校验状态
        if (order.getStatus() != TradeOrderStatus.PROGRESS) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.STATUS_INVALID);
        }

        // 4. 更新订单状态
        order.setStatus(TradeOrderStatus.ABANDONED);
        orderMapper.updateById(order);

        // 5. 减少任务接单人数
        taskClient.decreaseAcceptors(order.getTaskId());

        // 6. 检查是否还有其他进行中的订单，如果没有则恢复任务状态为招募中
        int progressCount = orderMapper.countByTaskIdAndStatus(order.getTaskId(), TradeOrderStatus.PROGRESS.getCode());
        if (progressCount == 0) {
            taskClient.updateTaskStatus(order.getTaskId(), TaskStatus.RECRUITING);
        }

        log.info("放弃任务: orderId={}, acceptorId={}", orderId, acceptorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long initiateAppeal(Long orderId, TradeAppealRequest request, Long userId) {
        // 1. 查询订单
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("订单"));
        }

        // 2. 校验权限（只有接单者或发布者可以申诉）
        boolean isAcceptor = order.getAcceptorId().equals(userId);
        boolean isPublisher = order.getPublisherId().equals(userId);
        if (!isAcceptor && !isPublisher) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.ACCESS_DENIED);
        }

        // 3. 校验状态（待确认或待收款状态可以申诉）
        if (order.getStatus() != TradeOrderStatus.SUBMIT && order.getStatus() != TradeOrderStatus.PAYMENT) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.STATUS_INVALID);
        }

        // 4. 检查是否已存在待处理的申诉
        int pendingAppeal = appealMapper.checkPendingAppeal(orderId);
        if (pendingAppeal > 0) {
            throw new BusinessException.DataAlreadyExistsException("已存在待处理的申诉");
        }

        // 5. 创建申诉记录
        TradeAppeal appeal = new TradeAppeal();
        appeal.setOrderId(orderId);
        appeal.setAppellantId(userId);
        appeal.setAppealType(isAcceptor ? TradeAppealType.ACCEPTOR : TradeAppealType.PUBLISHER);
        appeal.setReason(request.getReason());
        if (request.getEvidenceImages() != null && !request.getEvidenceImages().isEmpty()) {
            appeal.setEvidenceImages(JsonConvertMapstruct.stringListToJson(request.getEvidenceImages()));
        }
        appeal.setStatus(TradeAppealStatus.PENDING);
        appealMapper.insert(appeal);

        // 6. 更新订单状态为争议中
        order.setStatus(TradeOrderStatus.DISPUTE);
        orderMapper.updateById(order);

        // 7. 更新任务状态为争议中
        taskClient.updateTaskStatus(order.getTaskId(), TaskStatus.DISPUTE);

        log.info("发起申诉: orderId={}, appealId={}, userId={}", orderId, appeal.getId(), userId);

        // TODO: 争议处理的具体实现待后续完善
        return appeal.getId();
    }

    @Override
    public PageResult<TradeOrderVO> pageMyOrders(TradeOrderQuery query, Long userId) {
        int offset = (query.getPage() - 1) * query.getSize();
        List<TradeOrderDTO> dtoList = orderMapper.selectMyOrderPage(userId, query, offset, query.getSize());
        List<TradeOrderVO> records = orderMapstruct.toVOList(dtoList);
        long total = orderMapper.countMyOrder(userId, query);
        return PageResult.of(records, total, query.getPage(), query.getSize());
    }

    @Override
    public PageResult<TradeOrderVO> pageTaskOrders(Long taskId, TradeOrderQuery query, Long publisherId) {
        // 校验权限
        Boolean isPublisher = taskClient.isPublisher(taskId, publisherId);
        if (!Boolean.TRUE.equals(isPublisher)) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.ACCESS_DENIED);
        }

        int offset = (query.getPage() - 1) * query.getSize();
        List<TradeOrderDTO> dtoList = orderMapper.selectTaskOrderPage(taskId, query, offset, query.getSize());
        List<TradeOrderVO> records = orderMapstruct.toVOList(dtoList);
        long total = orderMapper.countTaskOrder(taskId, query);
        return PageResult.of(records, total, query.getPage(), query.getSize());
    }

    @Override
    public TradeOrderVO getOrderDetail(Long orderId, Long userId) {
        TradeOrderDTO dto = orderMapper.selectOrderDetail(orderId);
        if (dto == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("订单"));
        }

        // 校验权限（只有接单者或发布者可以查看）
        if (!dto.getAcceptorId().equals(userId) && !dto.getPublisherId().equals(userId)) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.ACCESS_DENIED);
        }

        return orderMapstruct.toVO(dto);
    }

    // TODO: 支付结算功能待后续与Pay_Service集成实现
    // public void completePayment(Long orderId) { ... }
}
