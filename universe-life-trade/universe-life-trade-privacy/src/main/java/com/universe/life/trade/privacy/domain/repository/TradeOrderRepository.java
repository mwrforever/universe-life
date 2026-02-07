package com.universe.life.trade.privacy.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.trade.privacy.domain.model.aggregate.TradeOrderAggregate;
import com.universe.life.trade.privacy.domain.model.valueobject.TradeOrderStatusEnum;

import java.util.List;
import java.util.Optional;

/**
 * 交易订单仓储接口（领域层）
 * <p>
 * 定义订单聚合根的持久化操作接口。
 * 遵循DDD仓储模式，隐藏底层持久化细节。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
public interface TradeOrderRepository {

    /**
     * 保存订单（新增或更新）
     *
     * @param order 订单聚合根
     * @return 保存后的订单聚合根
     */
    TradeOrderAggregate save(TradeOrderAggregate order);

    /**
     * 根据ID查询订单
     *
     * @param orderId 订单ID
     * @return 订单聚合根，如果不存在则返回空
     */
    Optional<TradeOrderAggregate> findById(Long orderId);

    /**
     * 根据任务ID和接单者ID查询订单
     * <p>用于检查用户是否已对某任务接单</p>
     *
     * @param taskId     任务ID
     * @param acceptorId 接单者ID
     * @return 订单聚合根，如果不存在则返回空
     */
    Optional<TradeOrderAggregate> findByTaskIdAndAcceptorId(Long taskId, Long acceptorId);

    /**
     * 分页查询接单者的订单列表
     *
     * @param acceptorId 接单者ID
     * @param status     订单状态（可选）
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @return 订单分页结果
     */
    Page<TradeOrderAggregate> pageByAcceptorId(Long acceptorId, TradeOrderStatusEnum status,
                                                 int pageNum, int pageSize);

    /**
     * 分页查询任务的订单列表
     *
     * @param taskId   任务ID
     * @param status   订单状态（可选）
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 订单分页结果
     */
    Page<TradeOrderAggregate> pageByTaskId(Long taskId, TradeOrderStatusEnum status,
                                             int pageNum, int pageSize);

    /**
     * 统计任务的订单数量
     *
     * @param taskId 任务ID
     * @param status 订单状态（可选）
     * @return 订单数量
     */
    long countByTaskIdAndStatus(Long taskId, TradeOrderStatusEnum status);

    /**
     * 根据任务ID查询所有订单
     * <p>用于任务取消时批量处理订单</p>
     *
     * @param taskId 任务ID
     * @return 订单列表
     */
    List<TradeOrderAggregate> findAllByTaskId(Long taskId);

    /**
     * 更新订单状态（使用乐观锁）
     *
     * @param orderId   订单ID
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @return 是否更新成功
     */
    boolean updateStatus(Long orderId, TradeOrderStatusEnum oldStatus, TradeOrderStatusEnum newStatus);

    /**
     * 删除订单（逻辑删除）
     *
     * @param orderId 订单ID
     * @return 是否删除成功
     */
    boolean deleteById(Long orderId);

    boolean lockForAppeal(Long orderId);

    boolean unlockForAppeal(Long orderId);
}
