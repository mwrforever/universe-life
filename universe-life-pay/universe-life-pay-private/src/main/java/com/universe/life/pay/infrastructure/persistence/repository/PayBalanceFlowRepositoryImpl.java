package com.universe.life.pay.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.pay.domain.repository.PayBalanceFlowRepository;
import com.universe.life.pay.infrastructure.persistence.mapper.PayBalanceFlowMapper;
import com.universe.life.pay.infrastructure.persistence.po.PayBalanceFlowPO;
import com.universe.life.pay.model.dto.AdminBalanceFlowListQueryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class PayBalanceFlowRepositoryImpl implements PayBalanceFlowRepository {

    private final PayBalanceFlowMapper mapper;

    @Override
    public PayBalanceFlowPO save(PayBalanceFlowPO po) {
        if (po.getId() == null) {
            mapper.insert(po);
        } else {
            mapper.updateById(po);
        }
        return po;
    }

    @Override
    public boolean existsByRequestNoUserAction(String requestNo, Long userId, String action) {
        if (requestNo == null || requestNo.isBlank() || userId == null || action == null || action.isBlank()) {
            return false;
        }
        LambdaQueryWrapper<PayBalanceFlowPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayBalanceFlowPO::getRequestNo, requestNo)
                .eq(PayBalanceFlowPO::getUserId, userId)
                .eq(PayBalanceFlowPO::getAction, action)
                .eq(PayBalanceFlowPO::getDeleted, 0)
                .last("limit 1");
        return mapper.selectOne(wrapper) != null;
    }

    @Override
    public IPage<PayBalanceFlowPO> pageAdminFlows(Page<PayBalanceFlowPO> page, AdminBalanceFlowListQueryDTO query) {
        if (page == null) {
            page = new Page<>(1, 20);
        }
        LambdaQueryWrapper<PayBalanceFlowPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayBalanceFlowPO::getDeleted, 0)
                .eq(query != null && query.getUserId() != null, PayBalanceFlowPO::getUserId, query.getUserId())
                .eq(query != null && StringUtils.hasText(query.getBizType()), PayBalanceFlowPO::getBizType, query.getBizType())
                .eq(query != null && query.getBizId() != null, PayBalanceFlowPO::getBizId, query.getBizId())
                .eq(query != null && StringUtils.hasText(query.getAction()), PayBalanceFlowPO::getAction, query.getAction())
                .ge(query != null && query.getStartTime() != null, PayBalanceFlowPO::getCreatedAt, query.getStartTime())
                .le(query != null && query.getEndTime() != null, PayBalanceFlowPO::getCreatedAt, query.getEndTime())
                .orderByDesc(PayBalanceFlowPO::getId);
        return mapper.selectPage(page, wrapper);
    }
}
