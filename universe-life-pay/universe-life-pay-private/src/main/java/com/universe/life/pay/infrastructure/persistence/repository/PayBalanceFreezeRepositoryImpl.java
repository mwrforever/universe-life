package com.universe.life.pay.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.universe.life.pay.domain.repository.PayBalanceFreezeRepository;
import com.universe.life.pay.infrastructure.persistence.mapper.PayBalanceFreezeMapper;
import com.universe.life.pay.infrastructure.persistence.po.PayBalanceFreezePO;
import com.universe.life.pay.model.enums.BalanceFreezeStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class PayBalanceFreezeRepositoryImpl implements PayBalanceFreezeRepository {

    private final PayBalanceFreezeMapper mapper;

    @Override
    public PayBalanceFreezePO save(PayBalanceFreezePO po) {
        if (po.getId() == null) {
            mapper.insert(po);
        } else {
            mapper.updateById(po);
        }
        return po;
    }

    @Override
    public Optional<PayBalanceFreezePO> findByFreezeNo(String freezeNo) {
        if (freezeNo == null || freezeNo.isBlank()) {
            return Optional.empty();
        }
        LambdaQueryWrapper<PayBalanceFreezePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayBalanceFreezePO::getFreezeNo, freezeNo)
                .eq(PayBalanceFreezePO::getDeleted, 0)
                .last("limit 1");
        return Optional.ofNullable(mapper.selectOne(wrapper));
    }

    @Override
    public Optional<PayBalanceFreezePO> findByRequestNoAndPayerId(String requestNo, Long payerId) {
        if (requestNo == null || requestNo.isBlank() || payerId == null) {
            return Optional.empty();
        }
        LambdaQueryWrapper<PayBalanceFreezePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayBalanceFreezePO::getRequestNo, requestNo)
                .eq(PayBalanceFreezePO::getPayerId, payerId)
                .eq(PayBalanceFreezePO::getDeleted, 0)
                .last("limit 1");
        return Optional.ofNullable(mapper.selectOne(wrapper));
    }

    @Override
    public Optional<PayBalanceFreezePO> findByBiz(String bizType, Long bizId, Long payerId) {
        if (bizType == null || bizType.isBlank() || bizId == null || payerId == null) {
            return Optional.empty();
        }
        LambdaQueryWrapper<PayBalanceFreezePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayBalanceFreezePO::getBizType, bizType)
                .eq(PayBalanceFreezePO::getBizId, bizId)
                .eq(PayBalanceFreezePO::getPayerId, payerId)
                .eq(PayBalanceFreezePO::getDeleted, 0)
                .last("limit 1");
        return Optional.ofNullable(mapper.selectOne(wrapper));
    }

    @Override
    public boolean markConfirmedDebit(Long id) {
        if (id == null) {
            return false;
        }
        Set<Integer> allowedFrom = Set.of(
                BalanceFreezeStatusEnum.FROZEN.getCode()
        );
        LambdaUpdateWrapper<PayBalanceFreezePO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PayBalanceFreezePO::getId, id)
                .eq(PayBalanceFreezePO::getDeleted, 0)
                .in(PayBalanceFreezePO::getStatus, allowedFrom)
                .set(PayBalanceFreezePO::getStatus, BalanceFreezeStatusEnum.CONFIRMED_DEBIT.getCode())
                .set(PayBalanceFreezePO::getUpdatedAt, LocalDateTime.now());
        return mapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean markUnfrozen(Long id) {
        if (id == null) {
            return false;
        }
        Set<Integer> allowedFrom = Set.of(
                BalanceFreezeStatusEnum.FROZEN.getCode()
        );
        LambdaUpdateWrapper<PayBalanceFreezePO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PayBalanceFreezePO::getId, id)
                .eq(PayBalanceFreezePO::getDeleted, 0)
                .in(PayBalanceFreezePO::getStatus, allowedFrom)
                .set(PayBalanceFreezePO::getStatus, BalanceFreezeStatusEnum.UNFROZEN.getCode())
                .set(PayBalanceFreezePO::getUpdatedAt, LocalDateTime.now());
        return mapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean markClosed(Long id) {
        if (id == null) {
            return false;
        }
        Set<Integer> allowedFrom = Set.of(
                BalanceFreezeStatusEnum.FROZEN.getCode()
        );
        LambdaUpdateWrapper<PayBalanceFreezePO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PayBalanceFreezePO::getId, id)
                .eq(PayBalanceFreezePO::getDeleted, 0)
                .in(PayBalanceFreezePO::getStatus, allowedFrom)
                .set(PayBalanceFreezePO::getStatus, BalanceFreezeStatusEnum.CLOSED.getCode())
                .set(PayBalanceFreezePO::getUpdatedAt, LocalDateTime.now());
        return mapper.update(null, wrapper) > 0;
    }
}
