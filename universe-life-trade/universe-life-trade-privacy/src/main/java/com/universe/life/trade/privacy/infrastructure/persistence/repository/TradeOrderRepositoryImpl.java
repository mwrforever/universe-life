package com.universe.life.trade.privacy.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.universe.life.trade.privacy.domain.model.aggregate.TradeOrderAggregate;
import com.universe.life.trade.privacy.domain.model.valueobject.TradeOrderStatusEnum;
import com.universe.life.trade.privacy.domain.repository.TradeOrderRepository;
import com.universe.life.trade.privacy.infrastructure.persistence.mapper.TradeOrderPOMapper;
import com.universe.life.trade.privacy.infrastructure.persistence.po.TradeOrderPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 交易订单仓储实现
 * <p>
 * 实现领域层定义的仓储接口，负责聚合根与持久化对象之间的转换。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TradeOrderRepositoryImpl implements TradeOrderRepository {

    private final TradeOrderPOMapper tradeOrderMapper;
    private final ObjectMapper objectMapper;

    @Override
    public TradeOrderAggregate save(TradeOrderAggregate order) {
        TradeOrderPO po = toTradeOrderPO(order);
        if (po.getId() == null) {
            tradeOrderMapper.insert(po);
            order.setId(po.getId());
        } else {
            tradeOrderMapper.updateById(po);
        }
        return order;
    }

    @Override
    public Optional<TradeOrderAggregate> findById(Long orderId) {
        TradeOrderPO po = tradeOrderMapper.selectById(orderId);
        return Optional.ofNullable(po).map(this::toTradeOrderAggregate);
    }

    @Override
    public Optional<TradeOrderAggregate> findByTaskIdAndAcceptorId(Long taskId, Long acceptorId) {
        LambdaQueryWrapper<TradeOrderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeOrderPO::getTaskId, taskId)
                .eq(TradeOrderPO::getAcceptorId, acceptorId);
        TradeOrderPO po = tradeOrderMapper.selectOne(wrapper);
        return Optional.ofNullable(po).map(this::toTradeOrderAggregate);
    }

    @Override
    public Page<TradeOrderAggregate> pageByAcceptorId(Long acceptorId, TradeOrderStatusEnum status,
                                                        int pageNum, int pageSize) {
        LambdaQueryWrapper<TradeOrderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeOrderPO::getAcceptorId, acceptorId);
        if (status != null) {
            wrapper.eq(TradeOrderPO::getStatus, status);
        }
        wrapper.orderByDesc(TradeOrderPO::getCreatedAt);

        Page<TradeOrderPO> poPage = new Page<>(pageNum, pageSize);
        tradeOrderMapper.selectPage(poPage, wrapper);

        Page<TradeOrderAggregate> result = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        result.setRecords(poPage.getRecords().stream()
                .map(this::toTradeOrderAggregate)
                .collect(Collectors.toList()));
        return result;
    }

    @Override
    public Page<TradeOrderAggregate> pageByTaskId(Long taskId, TradeOrderStatusEnum status,
                                                    int pageNum, int pageSize) {
        LambdaQueryWrapper<TradeOrderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeOrderPO::getTaskId, taskId);
        if (status != null) {
            wrapper.eq(TradeOrderPO::getStatus, status);
        }
        wrapper.orderByDesc(TradeOrderPO::getCreatedAt);

        Page<TradeOrderPO> poPage = new Page<>(pageNum, pageSize);
        tradeOrderMapper.selectPage(poPage, wrapper);

        Page<TradeOrderAggregate> result = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        result.setRecords(poPage.getRecords().stream()
                .map(this::toTradeOrderAggregate)
                .collect(Collectors.toList()));
        return result;
    }

    @Override
    public long countByTaskIdAndStatus(Long taskId, TradeOrderStatusEnum status) {
        LambdaQueryWrapper<TradeOrderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeOrderPO::getTaskId, taskId);
        if (status != null) {
            wrapper.eq(TradeOrderPO::getStatus, status);
        }
        return tradeOrderMapper.selectCount(wrapper);
    }

    @Override
    public List<TradeOrderAggregate> findAllByTaskId(Long taskId) {
        LambdaQueryWrapper<TradeOrderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeOrderPO::getTaskId, taskId);
        List<TradeOrderPO> poList = tradeOrderMapper.selectList(wrapper);
        return poList.stream()
                .map(this::toTradeOrderAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public boolean updateStatus(Long orderId, TradeOrderStatusEnum oldStatus, TradeOrderStatusEnum newStatus) {
        LambdaUpdateWrapper<TradeOrderPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TradeOrderPO::getId, orderId)
                .eq(TradeOrderPO::getStatus, oldStatus)
                .set(TradeOrderPO::getStatus, newStatus);
        return tradeOrderMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean deleteById(Long orderId) {
        return tradeOrderMapper.deleteById(orderId) > 0;
    }

    // ==================== 转换方法 ====================

    /**
     * 聚合根 → PO
     */
    private TradeOrderPO toTradeOrderPO(TradeOrderAggregate aggregate) {
        TradeOrderPO po = new TradeOrderPO();
        po.setId(aggregate.getIdValue());
        po.setTaskId(aggregate.getTaskId());
        po.setPublisherId(aggregate.getPublisherId());
        po.setAcceptorId(aggregate.getAcceptorId());
        po.setRewardAmount(aggregate.getRewardAmountCents());
        po.setStatus(aggregate.getStatus());
        
        // 提交成果
        if (aggregate.hasSubmitResult()) {
            po.setSubmitContent(aggregate.getSubmitResult().getContent());
            po.setSubmitImages(toJson(aggregate.getSubmitResult().getImages()));
        }
        
        // 拒绝信息
        if (aggregate.hasRejectInfo()) {
            po.setRejectReason(aggregate.getRejectInfo().getReason());
            po.setRejectedAt(aggregate.getRejectInfo().getRejectedAt());
            po.setRejectedBy(aggregate.getRejectInfo().getRejectedBy());
        }
        
        po.setAppliedAt(aggregate.getAppliedAt());
        po.setApprovedAt(aggregate.getApprovedAt());
        po.setSubmittedAt(aggregate.getSubmittedAt());
        po.setCompletedAt(aggregate.getCompletedAt());
        po.setCreatedAt(aggregate.getCreatedAt());
        po.setUpdatedAt(aggregate.getUpdatedAt());
        po.setVersion(aggregate.getVersion());
        return po;
    }

    /**
     * PO → 聚合根
     */
    private TradeOrderAggregate toTradeOrderAggregate(TradeOrderPO po) {
        return TradeOrderAggregate.reconstitute(
                po.getId(),
                po.getTaskId(),
                po.getPublisherId(),
                po.getAcceptorId(),
                po.getRewardAmount(),
                po.getStatus(),
                po.getSubmitContent(),
                fromJson(po.getSubmitImages()),
                po.getRejectReason(),
                po.getRejectedAt(),
                po.getRejectedBy(),
                po.getAppliedAt(),
                po.getApprovedAt(),
                po.getSubmittedAt(),
                po.getCompletedAt(),
                po.getCreatedAt(),
                po.getUpdatedAt(),
                po.getVersion()
        );
    }

    /**
     * List → JSON字符串
     */
    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.error("转换List到JSON失败", e);
            return null;
        }
    }

    /**
     * JSON字符串 → List
     */
    private List<String> fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("转换JSON到List失败: {}", json, e);
            return new ArrayList<>();
        }
    }
}
