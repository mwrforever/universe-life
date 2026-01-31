package com.universe.life.trade.privacy.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.auth.common.domain.Result;
import com.universe.life.common.domain.PageResult;
import com.universe.life.common.util.CacheUtil;
import com.universe.life.task.api.client.TaskClient;
import com.universe.life.task.model.dto.TaskInfoDTO;
import com.universe.life.task.model.enums.TaskStatus;
import com.universe.life.trade.privacy.application.assembler.TradeOrderAssembler;
import com.universe.life.trade.privacy.application.command.*;
import com.universe.life.trade.privacy.application.query.MyOrdersQuery;
import com.universe.life.trade.privacy.application.query.OrderDetailQuery;
import com.universe.life.trade.privacy.application.query.TaskOrdersQuery;
import com.universe.life.trade.privacy.domain.model.aggregate.TradeOrderAggregate;
import com.universe.life.trade.privacy.domain.model.valueobject.TradeOrderStatusEnum;
import com.universe.life.trade.privacy.domain.repository.TradeAppealRepository;
import com.universe.life.trade.privacy.domain.repository.TradeOrderRepository;
import com.universe.life.trade.privacy.infrastructure.constants.RedisKeyConstants;
import com.universe.life.trade.privacy.infrastructure.mq.TradeEventPublisher;
import com.universe.life.trade.privacy.interfaces.vo.TradeOrderFullDetailVO;
import com.universe.life.trade.privacy.interfaces.vo.TradeOrderSummaryVO;
import com.universe.life.trade.privacy.interfaces.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 交易订单应用服务
 * <p>
 * 负责交易订单相关的业务用例编排，包括：
 * <ul>
 *   <li>申请接单 - 用户申请接受某个任务</li>
 *   <li>审批接单 - 发布者同意或拒绝接单申请</li>
 *   <li>提交成果 - 接单者提交任务完成成果</li>
 *   <li>确认验收 - 发布者确认成果验收通过</li>
 *   <li>发起申诉 - 对订单结果发起申诉</li>
 *   <li>查询订单 - 订单详情、列表查询</li>
 * </ul>
 * </p>
 * <p>
 * 应用服务不包含业务规则，业务规则封装在领域层的聚合根中。
 * 应用服务负责：
 * <ul>
 *   <li>参数验证和权限检查</li>
 *   <li>调用领域服务和仓储</li>
 *   <li>事务管理</li>
 *   <li>领域事件发布</li>
 *   <li>对象转换（通过Assembler）</li>
 * </ul>
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeOrderApplicationService {

    /** 交易订单仓储 */
    private final TradeOrderRepository orderRepository;
    
    /** 申诉仓储 */
    private final TradeAppealRepository appealRepository;
    
    /** 订单对象装配器 */
    private final TradeOrderAssembler orderAssembler;
    
    /** 交易事件发布器 */
    private final TradeEventPublisher eventPublisher;
    
    /** 任务服务Feign客户端 */
    private final TaskClient taskClient;
    
    /** 缓存工具 */
    private final CacheUtil cacheUtil;

    /**
     * 申请接单
     * <p>
     * 业务流程：
     * <ol>
     *   <li>检查是否已申请过该任务</li>
     *   <li>调用任务服务获取任务信息并验证</li>
     *   <li>创建订单聚合根</li>
     *   <li>持久化订单数据</li>
     *   <li>调用任务服务增加接单人数</li>
     *   <li>发布订单创建事件</li>
     * </ol>
     * </p>
     *
     * @param cmd 申请接单命令
     * @return 新创建的订单ID
     * @throws IllegalStateException 当已申请过该任务、任务不存在或任务不可接单时抛出
     */
    @Transactional
    public Long applyOrder(ApplyOrderCommand cmd) {
        // 1. 检查是否已申请
        if (orderRepository.findByTaskIdAndAcceptorId(cmd.getTaskId(), cmd.getAcceptorId()).isPresent()) {
            throw new IllegalStateException("已申请过该任务");
        }

        // 2. 调用任务服务获取任务信息
        TaskInfoDTO taskInfo = getTaskInfoOrThrow(cmd.getTaskId());
        
        // 3. 验证任务状态是否可接单
        validateTaskCanAccept(taskInfo, cmd.getAcceptorId());

        // 4. 创建订单
        TradeOrderAggregate order = TradeOrderAggregate.create(
                cmd.getTaskId(), 
                taskInfo.getPublisherId(), 
                cmd.getAcceptorId(), 
                taskInfo.getRewardAmount());

        // 5. 保存订单
        order = orderRepository.save(order);

        // 6. 调用任务服务增加接单人数
        incrementTaskAcceptors(cmd.getTaskId());

        // 7. 发布领域事件
        order.pullDomainEvents().forEach(eventPublisher::publish);

        log.info("申请接单成功: orderId={}, taskId={}, acceptorId={}", 
                order.getIdValue(), cmd.getTaskId(), cmd.getAcceptorId());
        return order.getIdValue();
    }
    
    /**
     * 获取任务信息，如果任务不存在则抛出异常
     *
     * @param taskId 任务ID
     * @return 任务信息DTO
     * @throws IllegalArgumentException 当任务不存在或服务调用失败时抛出
     */
    private TaskInfoDTO getTaskInfoOrThrow(Long taskId) {
        Result<TaskInfoDTO> result = taskClient.getTaskInfo(taskId);
        
        // 检查服务调用是否成功（code=1表示成功）
        if (result.code() != 1) {
            log.error("调用任务服务失败: taskId={}, code={}, msg={}", 
                    taskId, result.code(), result.message());
            throw new IllegalStateException("任务服务暂时不可用，请稍后重试");
        }
        
        // 检查任务是否存在
        TaskInfoDTO taskInfo = result.data();
        if (taskInfo == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }
        
        return taskInfo;
    }
    
    /**
     * 验证任务是否可接单
     * <p>
     * 检查条件：
     * <ul>
     *   <li>任务状态必须为招募中</li>
     *   <li>接单人数未满</li>
     *   <li>不能接自己发布的任务</li>
     * </ul>
     * </p>
     *
     * @param taskInfo 任务信息
     * @param acceptorId 接单者ID
     * @throws IllegalStateException 当任务不可接单时抛出
     */
    private void validateTaskCanAccept(TaskInfoDTO taskInfo, Long acceptorId) {
        // 检查是否为自己发布的任务
        if (taskInfo.getPublisherId().equals(acceptorId)) {
            throw new IllegalStateException("不能接自己发布的任务");
        }
        
        // 检查任务状态
        if (taskInfo.getStatus() != TaskStatus.RECRUITING) {
            throw new IllegalStateException("任务当前状态不可接单");
        }
        
        // 检查接单人数是否已满
        if (taskInfo.getCurrentAcceptors() >= taskInfo.getMaxAcceptors()) {
            throw new IllegalStateException("任务接单人数已满");
        }
    }
    
    /**
     * 增加任务接单人数
     * <p>
     * 调用任务服务增加接单人数，如果调用失败会抛出异常触发事务回滚。
     * </p>
     *
     * @param taskId 任务ID
     */
    private void incrementTaskAcceptors(Long taskId) {
        Result<Void> result = taskClient.incrementAcceptors(taskId);
        if (result.code() != 1) {
            log.error("增加任务接单人数失败: taskId={}, code={}, msg={}", 
                    taskId, result.code(), result.message());
            throw new IllegalStateException("更新任务接单人数失败，请稍后重试");
        }
    }
    
    /**
     * 减少任务接单人数
     * <p>
     * 调用任务服务减少接单人数，如果调用失败会抛出异常触发事务回滚。
     * </p>
     *
     * @param taskId 任务ID
     */
    private void decrementTaskAcceptors(Long taskId) {
        Result<Void> result = taskClient.decrementAcceptors(taskId);
        if (result.code() != 1) {
            log.error("减少任务接单人数失败: taskId={}, code={}, msg={}", 
                    taskId, result.code(), result.message());
            throw new IllegalStateException("更新任务接单人数失败，请稍后重试");
        }
    }

    /**
     * 同意接单
     * <p>
     * 发布者同意接单申请，订单状态变为进行中。
     * </p>
     *
     * @param cmd 同意接单命令
     * @throws IllegalStateException 当无权操作或状态不允许时抛出
     */
    @Transactional
    public void approveOrder(ApproveOrderCommand cmd) {
        // 1. 获取订单聚合根
        TradeOrderAggregate order = getOrderById(cmd.getOrderId());
        
        // 2. 验证操作权限（只有发布者可以审批）
        if (!order.isPublisher(cmd.getOperatorId())) {
            throw new IllegalStateException("无权操作此订单");
        }
        
        // 3. 同意接单（聚合根内部检查状态）
        order.approve();
        
        // 4. 保存订单
        orderRepository.save(order);
        
        // 5. 删除订单详情缓存
        deleteOrderCache(cmd.getOrderId());
        
        // 6. 发布领域事件
        order.pullDomainEvents().forEach(eventPublisher::publish);
        
        log.info("同意接单成功: orderId={}", cmd.getOrderId());
    }

    /**
     * 拒绝接单
     * <p>
     * 发布者拒绝接单申请，需要提供拒绝原因。
     * 拒绝后会调用任务服务减少接单人数。
     * </p>
     *
     * @param cmd 拒绝接单命令
     * @throws IllegalStateException 当无权操作或状态不允许时抛出
     */
    @Transactional
    public void rejectOrder(RejectOrderCommand cmd) {
        // 1. 获取订单聚合根
        TradeOrderAggregate order = getOrderById(cmd.getOrderId());
        
        // 2. 验证操作权限
        if (!order.isPublisher(cmd.getOperatorId())) {
            throw new IllegalStateException("无权操作此订单");
        }
        
        // 3. 拒绝接单
        order.reject(cmd.getReason());
        
        // 4. 保存订单
        orderRepository.save(order);
        
        // 5. 删除订单详情缓存
        deleteOrderCache(cmd.getOrderId());
        
        // 6. 调用任务服务减少接单人数
        decrementTaskAcceptors(order.getTaskId());
        
        // 7. 发布领域事件
        order.pullDomainEvents().forEach(eventPublisher::publish);
        
        log.info("拒绝接单成功: orderId={}", cmd.getOrderId());
    }

    /**
     * 提交成果
     * <p>
     * 接单者提交任务完成成果，包括文字说明和图片证明。
     * 提交后订单状态变为待确认，等待发布者验收。
     * </p>
     *
     * @param cmd 提交成果命令
     * @throws IllegalStateException 当无权操作或状态不允许时抛出
     */
    @Transactional
    public void submitResult(SubmitResultCommand cmd) {
        // 1. 获取订单聚合根
        TradeOrderAggregate order = getOrderById(cmd.getOrderId());
        
        // 2. 验证操作权限（只有接单者可以提交成果）
        if (!order.isAcceptor(cmd.getOperatorId())) {
            throw new IllegalStateException("无权操作此订单");
        }
        
        // 3. 提交成果
        order.submit(cmd.getContent(), cmd.getImages());
        
        // 4. 保存订单
        orderRepository.save(order);
        
        // 5. 删除订单详情缓存
        deleteOrderCache(cmd.getOrderId());
        
        // 6. 发布领域事件
        order.pullDomainEvents().forEach(eventPublisher::publish);
        
        log.info("提交成果成功: orderId={}", cmd.getOrderId());
    }

    /**
     * 确认验收
     * <p>
     * 发布者确认成果验收通过，订单进入待收款状态。
     * </p>
     *
     * @param cmd 确认验收命令
     * @throws IllegalStateException 当无权操作或状态不允许时抛出
     */
    @Transactional
    public void confirmResult(ConfirmResultCommand cmd) {
        // 1. 获取订单聚合根
        TradeOrderAggregate order = getOrderById(cmd.getOrderId());
        
        // 2. 验证操作权限（只有发布者可以确认验收）
        if (!order.isPublisher(cmd.getOperatorId())) {
            throw new IllegalStateException("无权操作此订单");
        }
        
        // 3. 确认验收
        order.confirm();
        
        // 4. 保存订单
        orderRepository.save(order);
        
        // 5. 删除订单详情缓存
        deleteOrderCache(cmd.getOrderId());
        
        // 6. 发布领域事件
        order.pullDomainEvents().forEach(eventPublisher::publish);
        
        log.info("确认验收成功: orderId={}", cmd.getOrderId());
    }

    /**
     * 放弃任务
     * <p>
     * 接单者主动放弃任务，订单状态变为已放弃。
     * 放弃任务可能会影响接单者的信用评分。
     * 放弃后会调用任务服务减少接单人数。
     * </p>
     *
     * @param cmd 放弃任务命令
     * @throws IllegalStateException 当无权操作或状态不允许时抛出
     */
    @Transactional
    public void abandonOrder(AbandonOrderCommand cmd) {
        // 1. 获取订单聚合根
        TradeOrderAggregate order = getOrderById(cmd.getOrderId());
        
        // 2. 验证操作权限（只有接单者可以放弃）
        if (!order.isAcceptor(cmd.getOperatorId())) {
            throw new IllegalStateException("无权操作此订单");
        }
        
        // 3. 放弃任务
        order.abandon();
        
        // 4. 保存订单
        orderRepository.save(order);
        
        // 5. 删除订单详情缓存
        deleteOrderCache(cmd.getOrderId());
        
        // 6. 调用任务服务减少接单人数
        decrementTaskAcceptors(order.getTaskId());
        
        // 7. 发布领域事件
        order.pullDomainEvents().forEach(eventPublisher::publish);
        
        log.info("放弃任务成功: orderId={}", cmd.getOrderId());
    }

    /**
     * 发起申诉
     * <p>
     * 对订单结果发起申诉，订单状态变为争议中。
     * 同一订单只能有一个待处理的申诉。
     * </p>
     *
     * @param cmd 发起申诉命令
     * @throws IllegalStateException 当无权操作、状态不允许或已存在待处理申诉时抛出
     */
    @Transactional
    public void initiateAppeal(InitiateAppealCommand cmd) {
        // 1. 获取订单聚合根
        TradeOrderAggregate order = getOrderById(cmd.getOrderId());
        
        // 2. 验证操作权限（发布者和接单者都可以发起申诉）
        if (!order.canOperate(cmd.getOperatorId())) {
            throw new IllegalStateException("无权操作此订单");
        }
        
        // 3. 检查是否已存在待处理的申诉
        if (appealRepository.existsPendingByOrderId(cmd.getOrderId())) {
            throw new IllegalStateException("已存在待处理的申诉");
        }
        
        // 4. 发起申诉
        order.dispute();
        
        // 5. 保存订单
        orderRepository.save(order);
        
        // 6. 删除订单详情缓存
        deleteOrderCache(cmd.getOrderId());
        
        // 7. 发布领域事件
        order.pullDomainEvents().forEach(eventPublisher::publish);
        
        log.info("发起申诉成功: orderId={}", cmd.getOrderId());
    }

    /**
     * 查询订单详情
     * <p>
     * 返回订单的完整信息，包括：
     * <ul>
     *   <li>订单基本信息</li>
     *   <li>任务信息</li>
     *   <li>发布者和接单者信息</li>
     *   <li>提交成果信息</li>
     *   <li>申诉信息</li>
     *   <li>时间线</li>
     *   <li>当前用户的可用操作</li>
     * </ul>
     * </p>
     *
     * @param query 订单详情查询参数
     * @return 订单完整详情VO
     * @throws IllegalStateException 当无权访问时抛出
     */
    public TradeOrderFullDetailVO getOrderDetail(OrderDetailQuery query) {
        // 1. 构建缓存键
        String cacheKey = RedisKeyConstants.buildOrderDetailKey(query.getOrderId());
        
        // 2. 从缓存获取或计算（30分钟 + 随机10-30分钟）
        return cacheUtil.getOrComputeWithRandomExpire(
                cacheKey,
                TradeOrderFullDetailVO.class,
                () -> {
                    // 获取订单聚合根
                    TradeOrderAggregate order = getOrderById(query.getOrderId());
                    
                    // 验证访问权限（只有发布者和接单者可以查看）
                    if (!order.canOperate(query.getCurrentUserId())) {
                        throw new IllegalStateException("无权访问此订单");
                    }
                    
                    // 获取用户信息（TODO: 调用用户服务）
                    UserInfoVO publisher = buildUserInfo(order.getPublisherId());
                    UserInfoVO acceptor = buildUserInfo(order.getAcceptorId());
                    
                    // 转换为VO
                    return orderAssembler.toDetailVO(order, "任务标题", "任务描述", null,
                            publisher, acceptor, null, query.getCurrentUserId());
                },
                30 // 基础过期时间30分钟
        );
    }

    /**
     * 查询我的接单列表
     * <p>
     * 分页查询当前用户作为接单者的订单列表，支持按状态筛选。
     * </p>
     *
     * @param query 我的接单查询参数
     * @return 分页结果
     */
    public PageResult<TradeOrderSummaryVO> pageMyOrders(MyOrdersQuery query) {
        int pageNum = query.getPageNumOrDefault();
        int pageSize = query.getPageSizeOrDefault();
        
        // 1. 转换状态参数
        TradeOrderStatusEnum status = query.getStatus() != null ?
                TradeOrderStatusEnum.of(query.getStatus()) : null;

        // 2. 查询订单列表
        Page<TradeOrderAggregate> page = orderRepository.pageByAcceptorId(
                query.getAcceptorId(), status, pageNum, pageSize);
        
        // 3. 获取订单列表和总数
        List<TradeOrderAggregate> orders = page.getRecords();
        long total = page.getTotal();

        // 4. 转换为VO
        List<TradeOrderSummaryVO> voList = orders.stream()
                .map(order -> orderAssembler.toSummaryVO(order, "任务标题", null, "发布者", null))
                .collect(Collectors.toList());
        
        return PageResult.of(voList, total, pageNum, pageSize);
    }

    /**
     * 查询任务的接单列表
     * <p>
     * 分页查询某个任务的所有接单申请，供发布者查看和审批。
     * </p>
     *
     * @param query 任务接单查询参数
     * @return 分页结果
     */
    public PageResult<TradeOrderSummaryVO> pageTaskOrders(TaskOrdersQuery query) {
        int pageNum = query.getPageNumOrDefault();
        int pageSize = query.getPageSizeOrDefault();
        
        // 1. 转换状态参数
        TradeOrderStatusEnum status = query.getStatus() != null ?
                TradeOrderStatusEnum.of(query.getStatus()) : null;

        // 2. 查询订单列表
        Page<TradeOrderAggregate> page = orderRepository.pageByTaskId(
                query.getTaskId(), status, pageNum, pageSize);
        
        // 3. 获取订单列表和总数
        List<TradeOrderAggregate> orders = page.getRecords();
        long total = page.getTotal();

        // 4. 转换为VO
        List<TradeOrderSummaryVO> voList = orders.stream()
                .map(order -> orderAssembler.toSummaryVO(order, "任务标题", null, "发布者", null))
                .collect(Collectors.toList());
        
        return PageResult.of(voList, total, pageNum, pageSize);
    }

    /**
     * 根据ID获取订单聚合根
     *
     * @param orderId 订单ID
     * @return 订单聚合根
     * @throws IllegalArgumentException 当订单不存在时抛出
     */
    private TradeOrderAggregate getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + orderId));
    }

    /**
     * 构建用户信息
     * <p>
     * 临时方法，后续需要调用用户服务获取真实的用户信息。
     * </p>
     *
     * @param userId 用户ID
     * @return 用户信息VO
     */
    private UserInfoVO buildUserInfo(Long userId) {
        // TODO: 调用用户服务获取真实用户信息
        UserInfoVO user = new UserInfoVO();
        user.setUserId(userId);
        user.setNickname("用户" + userId);
        user.setAvatar(null);
        return user;
    }
    
    /**
     * 删除订单详情缓存
     * <p>
     * 当订单状态发生变化时，删除缓存以确保数据一致性。
     * </p>
     *
     * @param orderId 订单ID
     */
    private void deleteOrderCache(Long orderId) {
        String cacheKey = RedisKeyConstants.buildOrderDetailKey(orderId);
        cacheUtil.delete(cacheKey);
        log.debug("删除订单详情缓存: orderId={}", orderId);
    }
}
