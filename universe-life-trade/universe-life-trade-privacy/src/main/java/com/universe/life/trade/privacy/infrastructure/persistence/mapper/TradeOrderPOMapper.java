package com.universe.life.trade.privacy.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.trade.privacy.infrastructure.persistence.po.TradeOrderPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 交易订单PO Mapper接口
 * <p>
 * 继承MyBatis-Plus的BaseMapper，提供基础的CRUD操作。
 * 自定义方法通过XML映射文件实现复杂SQL查询。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Mapper
public interface TradeOrderPOMapper extends BaseMapper<TradeOrderPO> {
    
    /**
     * 分页查询我的接单列表
     *
     * @param acceptorId 接单者ID
     * @param status 订单状态（可选）
     * @param taskId 任务ID（可选）
     * @param offset 偏移量
     * @param size 每页大小
     * @return 订单列表
     */
    List<TradeOrderPO> selectMyOrderPage(
            @Param("acceptorId") Long acceptorId,
            @Param("status") Integer status,
            @Param("taskId") Long taskId,
            @Param("offset") long offset,
            @Param("size") int size
    );
    
    /**
     * 统计我的接单数量
     *
     * @param acceptorId 接单者ID
     * @param status 订单状态（可选）
     * @param taskId 任务ID（可选）
     * @return 订单数量
     */
    long countMyOrder(
            @Param("acceptorId") Long acceptorId,
            @Param("status") Integer status,
            @Param("taskId") Long taskId
    );
    
    /**
     * 分页查询任务的接单列表
     *
     * @param taskId 任务ID
     * @param status 订单状态（可选）
     * @param offset 偏移量
     * @param size 每页大小
     * @return 订单列表
     */
    List<TradeOrderPO> selectTaskOrderPage(
            @Param("taskId") Long taskId,
            @Param("status") Integer status,
            @Param("offset") long offset,
            @Param("size") int size
    );
    
    /**
     * 统计任务的接单数量
     *
     * @param taskId 任务ID
     * @param status 订单状态（可选）
     * @return 订单数量
     */
    long countTaskOrder(
            @Param("taskId") Long taskId,
            @Param("status") Integer status
    );

    /**
     * 根据任务ID查询所有订单
     *
     * @param taskId 任务ID
     * @return 订单列表
     */
    @Select("SELECT * FROM trade_order WHERE task_id = #{taskId}")
    List<TradeOrderPO> selectByTaskId(@Param("taskId") Long taskId);
}
