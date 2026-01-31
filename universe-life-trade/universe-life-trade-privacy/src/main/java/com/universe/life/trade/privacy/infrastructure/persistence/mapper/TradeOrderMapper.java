package com.universe.life.trade.privacy.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.trade.privacy.infrastructure.persistence.po.TradeOrderPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 交易订单 Mapper
 * <p>
 * 基于 MyBatis Plus 的数据访问接口。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Mapper
public interface TradeOrderMapper extends BaseMapper<TradeOrderPO> {
    /**
     * 根据任务ID查询所有订单
     */
    @Select("SELECT * FROM trade_order WHERE task_id = #{taskId}")
    List<TradeOrderPO> selectByTaskId(@Param("taskId") Long taskId);
}
