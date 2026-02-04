package com.universe.life.trade.privacy.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.trade.privacy.domain.repository.TradeAppealRepository;
import com.universe.life.trade.privacy.infrastructure.enums.TradeAppealStatus;
import com.universe.life.trade.privacy.infrastructure.persistence.mapper.TradeAppealPOMapper;
import com.universe.life.trade.privacy.infrastructure.persistence.po.TradeAppealPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 交易申诉仓储实现
 * <p>
 * 实现TradeAppealRepository接口，负责交易申诉数据的持久化操作。
 * 使用MyBatis-Plus进行数据库操作。
 * </p>
 * 
 * <p>注意：当前申诉功能暂未实现完整的领域模型，直接使用PO进行操作。</p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class TradeAppealRepositoryImpl implements TradeAppealRepository {

    /** 交易申诉PO Mapper */
    private final TradeAppealPOMapper appealMapper;

    /**
     * 保存申诉记录
     * <p>
     * 根据申诉ID是否存在判断是新增还是更新操作。
     * 新增时会自动生成ID并回填到PO对象。
     * </p>
     *
     * @param appeal 申诉PO对象
     * @return 保存后的申诉PO（包含ID）
     */
    @Override
    public TradeAppealPO save(TradeAppealPO appeal) {
        if (appeal.getId() == null) {
            // 新增申诉
            appealMapper.insert(appeal);
        } else {
            // 更新申诉
            appealMapper.updateById(appeal);
        }
        return appeal;
    }

    /**
     * 根据ID查询申诉
     *
     * @param appealId 申诉ID
     * @return 申诉PO（Optional包装）
     */
    @Override
    public Optional<TradeAppealPO> findById(Long appealId) {
        return Optional.ofNullable(appealMapper.selectById(appealId));
    }

    /**
     * 根据订单ID查询申诉列表
     * <p>
     * 查询指定订单的所有申诉记录，按创建时间倒序排列。
     * </p>
     *
     * @param orderId 订单ID
     * @return 申诉列表
     */
    @Override
    public List<TradeAppealPO> findByOrderId(Long orderId) {
        LambdaQueryWrapper<TradeAppealPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeAppealPO::getOrderId, orderId)
                .orderByDesc(TradeAppealPO::getCreatedAt);
        return appealMapper.selectList(wrapper);
    }

    /**
     * 根据订单ID查询最新申诉
     * <p>
     * 获取指定订单的最近一次申诉记录。
     * </p>
     *
     * @param orderId 订单ID
     * @return 最新申诉PO（Optional包装）
     */
    @Override
    public Optional<TradeAppealPO> findLatestByOrderId(Long orderId) {
        LambdaQueryWrapper<TradeAppealPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeAppealPO::getOrderId, orderId)
                .orderByDesc(TradeAppealPO::getCreatedAt)
                .last("LIMIT 1");
        return Optional.ofNullable(appealMapper.selectOne(wrapper));
    }

    /**
     * 检查订单是否存在待处理的申诉
     * <p>
     * 用于防止用户在有未处理申诉时重复提交。
     * </p>
     *
     * @param orderId 订单ID
     * @return 如果存在待处理申诉返回true
     */
    @Override
    public boolean existsPendingByOrderId(Long orderId) {
        LambdaQueryWrapper<TradeAppealPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeAppealPO::getOrderId, orderId)
                .eq(TradeAppealPO::getStatus, TradeAppealStatus.PENDING);
        return appealMapper.selectCount(wrapper) > 0;
    }

    /**
     * 分页查询待处理的申诉列表
     * <p>
     * 供管理员查看和处理待处理的申诉，按创建时间升序排列（先提交的先处理）。
     * </p>
     *
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页数量
     * @return 待处理申诉列表
     */
    @Override
    public List<TradeAppealPO> findPendingAppeals(int pageNum, int pageSize) {
        LambdaQueryWrapper<TradeAppealPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeAppealPO::getStatus, TradeAppealStatus.PENDING)
                .orderByAsc(TradeAppealPO::getCreatedAt);

        Page<TradeAppealPO> page = new Page<>(pageNum, pageSize);
        return appealMapper.selectPage(page, wrapper).getRecords();
    }

    /**
     * 统计待处理的申诉数量
     * <p>
     * 用于管理后台显示待处理申诉数量。
     * </p>
     *
     * @return 待处理申诉数量
     */
    @Override
    public long countPendingAppeals() {
        LambdaQueryWrapper<TradeAppealPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeAppealPO::getStatus, TradeAppealStatus.PENDING);
        return appealMapper.selectCount(wrapper);
    }
}
