package com.universe.life.trade.privacy.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.trade.privacy.infrastructure.persistence.po.TradeAppealPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 交易申诉PO Mapper接口
 * <p>
 * 继承MyBatis-Plus的BaseMapper，提供基础的CRUD操作。
 * 自定义方法通过XML映射文件实现复杂SQL查询。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Mapper
public interface TradeAppealPOMapper extends BaseMapper<TradeAppealPO> {
    
    /**
     * 根据订单ID查询申诉列表
     *
     * @param orderId 订单ID
     * @return 申诉列表
     */
    List<TradeAppealPO> selectByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 分页查询申诉列表
     *
     * @param status 申诉状态（可选）
     * @param offset 偏移量
     * @param size 每页大小
     * @return 申诉列表
     */
    List<TradeAppealPO> selectAppealPage(
            @Param("status") Integer status,
            @Param("offset") long offset,
            @Param("size") int size
    );
    
    /**
     * 统计申诉数量
     *
     * @param status 申诉状态（可选）
     * @return 申诉数量
     */
    long countAppeal(@Param("status") Integer status);
}
