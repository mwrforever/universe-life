package com.universe.life.trade.privacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.trade.privacy.domain.po.TradeAppeal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 交易申诉Mapper接口
 *
 * @author universe-life
 */
@Mapper
public interface TradeAppealMapper extends BaseMapper<TradeAppeal> {

    /**
     * 检查订单是否存在待处理的申诉
     *
     * @param orderId 订单ID
     * @return 待处理申诉数量
     */
    int checkPendingAppeal(@Param("orderId") Long orderId);
}
