package com.universe.life.pay.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.universe.life.pay.domain.repository.PayBalanceAccountRepository;
import com.universe.life.pay.infrastructure.persistence.mapper.PayBalanceAccountMapper;
import com.universe.life.pay.infrastructure.persistence.po.PayBalanceAccountPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PayBalanceAccountRepositoryImpl implements PayBalanceAccountRepository {

    private final PayBalanceAccountMapper mapper;

    @Override
    public PayBalanceAccountPO save(PayBalanceAccountPO po) {
        if (po.getId() == null) {
            mapper.insert(po);
        } else {
            mapper.updateById(po);
        }
        return po;
    }

    @Override
    public Optional<PayBalanceAccountPO> findByUserId(Long userId) {
        return findByUserId(userId, "DEFAULT", "CNY");
    }

    @Override
    public Optional<PayBalanceAccountPO> findByUserId(Long userId, String accountType, String currency) {
        if (userId == null) {
            return Optional.empty();
        }
        LambdaQueryWrapper<PayBalanceAccountPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayBalanceAccountPO::getUserId, userId)
                .eq(PayBalanceAccountPO::getAccountType, accountType)
                .eq(PayBalanceAccountPO::getCurrency, currency)
                .eq(PayBalanceAccountPO::getDeleted, 0)
                .last("limit 1");
        return Optional.ofNullable(mapper.selectOne(wrapper));
    }

    @Override
    public boolean freeze(Long userId, String accountType, String currency, Long amount, Long version) {
        if (userId == null || amount == null || amount <= 0 || version == null) {
            return false;
        }
        LambdaUpdateWrapper<PayBalanceAccountPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PayBalanceAccountPO::getUserId, userId)
                .eq(PayBalanceAccountPO::getAccountType, accountType)
                .eq(PayBalanceAccountPO::getCurrency, currency)
                .eq(PayBalanceAccountPO::getDeleted, 0)
                .eq(PayBalanceAccountPO::getVersion, version)
                .ge(PayBalanceAccountPO::getAvailableBalance, amount)
                .setSql("available_balance = available_balance - " + amount)
                .setSql("frozen_balance = frozen_balance + " + amount)
                .setSql("version = version + 1")
                .set(PayBalanceAccountPO::getUpdatedAt, LocalDateTime.now());
        return mapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean unfreeze(Long userId, String accountType, String currency, Long amount, Long version) {
        if (userId == null || amount == null || amount <= 0 || version == null) {
            return false;
        }
        LambdaUpdateWrapper<PayBalanceAccountPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PayBalanceAccountPO::getUserId, userId)
                .eq(PayBalanceAccountPO::getAccountType, accountType)
                .eq(PayBalanceAccountPO::getCurrency, currency)
                .eq(PayBalanceAccountPO::getDeleted, 0)
                .eq(PayBalanceAccountPO::getVersion, version)
                .ge(PayBalanceAccountPO::getFrozenBalance, amount)
                .setSql("available_balance = available_balance + " + amount)
                .setSql("frozen_balance = frozen_balance - " + amount)
                .setSql("version = version + 1")
                .set(PayBalanceAccountPO::getUpdatedAt, LocalDateTime.now());
        return mapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean confirmDebit(Long userId, String accountType, String currency, Long amount, Long version) {
        if (userId == null || amount == null || amount <= 0 || version == null) {
            return false;
        }
        LambdaUpdateWrapper<PayBalanceAccountPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PayBalanceAccountPO::getUserId, userId)
                .eq(PayBalanceAccountPO::getAccountType, accountType)
                .eq(PayBalanceAccountPO::getCurrency, currency)
                .eq(PayBalanceAccountPO::getDeleted, 0)
                .eq(PayBalanceAccountPO::getVersion, version)
                .ge(PayBalanceAccountPO::getFrozenBalance, amount)
                .setSql("frozen_balance = frozen_balance - " + amount)
                .setSql("version = version + 1")
                .set(PayBalanceAccountPO::getUpdatedAt, LocalDateTime.now());
        return mapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean credit(Long userId, String accountType, String currency, Long amount, Long version) {
        if (userId == null || amount == null || amount <= 0 || version == null) {
            return false;
        }
        LambdaUpdateWrapper<PayBalanceAccountPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PayBalanceAccountPO::getUserId, userId)
                .eq(PayBalanceAccountPO::getAccountType, accountType)
                .eq(PayBalanceAccountPO::getCurrency, currency)
                .eq(PayBalanceAccountPO::getDeleted, 0)
                .eq(PayBalanceAccountPO::getVersion, version)
                .setSql("available_balance = available_balance + " + amount)
                .setSql("version = version + 1")
                .set(PayBalanceAccountPO::getUpdatedAt, LocalDateTime.now());
        return mapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean debit(Long userId, String accountType, String currency, Long amount, Long version) {
        if (userId == null || amount == null || amount <= 0 || version == null) {
            return false;
        }
        LambdaUpdateWrapper<PayBalanceAccountPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PayBalanceAccountPO::getUserId, userId)
                .eq(PayBalanceAccountPO::getAccountType, accountType)
                .eq(PayBalanceAccountPO::getCurrency, currency)
                .eq(PayBalanceAccountPO::getDeleted, 0)
                .eq(PayBalanceAccountPO::getVersion, version)
                .ge(PayBalanceAccountPO::getAvailableBalance, amount)
                .setSql("available_balance = available_balance - " + amount)
                .setSql("version = version + 1")
                .set(PayBalanceAccountPO::getUpdatedAt, LocalDateTime.now());
        return mapper.update(null, wrapper) > 0;
    }
}
