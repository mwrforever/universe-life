package com.universe.life.trade.privacy.domain.repository;

import com.universe.life.trade.privacy.infrastructure.persistence.po.TradeAppealPO;

import java.util.List;
import java.util.Optional;

/**
 * 交易申诉仓储接口
 */
public interface TradeAppealRepository {

    /**
     * 保存申诉
     *
     * @param appeal 申诉PO
     * @return 保存后的申诉
     */
    TradeAppealPO save(TradeAppealPO appeal);

    /**
     * 根据ID查询申诉
     *
     * @param appealId 申诉ID
     * @return 申诉PO
     */
    Optional<TradeAppealPO> findById(Long appealId);

    /**
     * 根据订单ID查询申诉列表
     *
     * @param orderId 订单ID
     * @return 申诉列表
     */
    List<TradeAppealPO> findByOrderId(Long orderId);

    /**
     * 根据订单ID查询最新申诉
     *
     * @param orderId 订单ID
     * @return 最新申诉
     */
    Optional<TradeAppealPO> findLatestByOrderId(Long orderId);

    /**
     * 检查订单是否存在待处理的申诉
     *
     * @param orderId 订单ID
     * @return 是否存在
     */
    boolean existsPendingByOrderId(Long orderId);

    /**
     * 查询待处理的申诉列表（管理员用）
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 申诉列表
     */
    List<TradeAppealPO> findPendingAppeals(int pageNum, int pageSize);

    /**
     * 统计待处理的申诉数量
     *
     * @return 数量
     */
    long countPendingAppeals();
}
