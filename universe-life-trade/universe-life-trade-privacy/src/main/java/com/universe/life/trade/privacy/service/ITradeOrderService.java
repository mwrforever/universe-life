package com.universe.life.trade.privacy.service;

import com.universe.life.common.domain.PageResult;
import com.universe.life.trade.privacy.domain.dao.query.TradeOrderQuery;
import com.universe.life.trade.privacy.domain.dto.request.*;
import com.universe.life.trade.privacy.domain.vo.TradeOrderVO;

/**
 * 交易订单服务接口
 *
 * @author universe-life
 */
public interface ITradeOrderService {

    /**
     * 创建交易订单（申请接单）
     *
     * @param taskId     需求ID
     * @param acceptorId 接单者ID
     * @return 订单ID
     */
    Long createOrder(Long taskId, Long acceptorId);

    /**
     * 同意接单申请
     *
     * @param orderId     订单ID
     * @param publisherId 发布者ID
     */
    void approveOrder(Long orderId, Long publisherId);

    /**
     * 拒绝接单申请
     *
     * @param orderId     订单ID
     * @param request     拒绝请求
     * @param publisherId 发布者ID
     */
    void rejectOrder(Long orderId, TradeRejectRequest request, Long publisherId);

    /**
     * 提交任务成果
     *
     * @param orderId    订单ID
     * @param request    提交请求
     * @param acceptorId 接单者ID
     */
    void submitResult(Long orderId, TradeSubmitRequest request, Long acceptorId);

    /**
     * 确认验收
     *
     * @param orderId     订单ID
     * @param request     确认请求
     * @param publisherId 发布者ID
     */
    void confirmResult(Long orderId, TradeConfirmRequest request, Long publisherId);

    /**
     * 放弃任务
     *
     * @param orderId    订单ID
     * @param acceptorId 接单者ID
     */
    void abandonOrder(Long orderId, Long acceptorId);

    /**
     * 发起申诉
     *
     * @param orderId 订单ID
     * @param request 申诉请求
     * @param userId  用户ID
     * @return 申诉ID
     */
    Long initiateAppeal(Long orderId, TradeAppealRequest request, Long userId);

    /**
     * 分页查询我的接单列表
     *
     * @param query  查询条件
     * @param userId 用户ID
     * @return 订单分页列表
     */
    PageResult<TradeOrderVO> pageMyOrders(TradeOrderQuery query, Long userId);

    /**
     * 分页查询需求的接单列表
     *
     * @param taskId      需求ID
     * @param query       查询条件
     * @param publisherId 发布者ID
     * @return 订单分页列表
     */
    PageResult<TradeOrderVO> pageTaskOrders(Long taskId, TradeOrderQuery query, Long publisherId);

    /**
     * 获取订单详情
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 订单详情
     */
    TradeOrderVO getOrderDetail(Long orderId, Long userId);
}
