package com.universe.life.trade.privacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.trade.privacy.domain.dto.TradeOrderDTO;
import com.universe.life.trade.privacy.domain.dao.query.TradeOrderQuery;
import com.universe.life.trade.privacy.domain.po.TradeOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 交易订单Mapper接口
 *
 * @author universe-life
 */
@Mapper
public interface TradeOrderMapper extends BaseMapper<TradeOrder> {

    /**
     * 检查用户是否已对某需求接单（存在未完结的订单）
     *
     * @param taskId     需求ID
     * @param acceptorId 接单者ID
     * @return 存在的订单数量
     */
    int checkUserAccepted(@Param("taskId") Long taskId, @Param("acceptorId") Long acceptorId);

    /**
     * 分页查询我的接单列表
     *
     * @param acceptorId 接单者ID
     * @param query      查询条件
     * @param offset     偏移量
     * @param size       每页数量
     * @return 订单列表
     */
    List<TradeOrderDTO> selectMyOrderPage(@Param("acceptorId") Long acceptorId,
                                          @Param("query") TradeOrderQuery query,
                                          @Param("offset") int offset,
                                          @Param("size") int size);

    /**
     * 统计我的接单数量
     *
     * @param acceptorId 接单者ID
     * @param query      查询条件
     * @return 订单数量
     */
    long countMyOrder(@Param("acceptorId") Long acceptorId, @Param("query") TradeOrderQuery query);

    /**
     * 分页查询需求的接单列表
     *
     * @param taskId 需求ID
     * @param query  查询条件
     * @param offset 偏移量
     * @param size   每页数量
     * @return 订单列表
     */
    List<TradeOrderDTO> selectTaskOrderPage(@Param("taskId") Long taskId,
                                            @Param("query") TradeOrderQuery query,
                                            @Param("offset") int offset,
                                            @Param("size") int size);

    /**
     * 统计需求的接单数量
     *
     * @param taskId 需求ID
     * @param query  查询条件
     * @return 订单数量
     */
    long countTaskOrder(@Param("taskId") Long taskId, @Param("query") TradeOrderQuery query);

    /**
     * 查询订单详情（包含关联信息）
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    TradeOrderDTO selectOrderDetail(@Param("orderId") Long orderId);

    /**
     * 统计需求下指定状态的订单数量
     *
     * @param taskId 需求ID
     * @param status 状态码
     * @return 订单数量
     */
    int countByTaskIdAndStatus(@Param("taskId") Long taskId, @Param("status") Integer status);
}
