package com.universe.life.pay.domain.repository;

import com.universe.life.pay.infrastructure.persistence.po.PayBalanceAccountPO;

import java.util.Optional;

public interface PayBalanceAccountRepository {

    PayBalanceAccountPO save(PayBalanceAccountPO po);

    Optional<PayBalanceAccountPO> findByUserId(Long userId);

    Optional<PayBalanceAccountPO> findByUserId(Long userId, String accountType, String currency);

    boolean freeze(Long userId, String accountType, String currency, Long amount, Long version);

    boolean unfreeze(Long userId, String accountType, String currency, Long amount, Long version);

    boolean confirmDebit(Long userId, String accountType, String currency, Long amount, Long version);

    boolean credit(Long userId, String accountType, String currency, Long amount, Long version);

    boolean debit(Long userId, String accountType, String currency, Long amount, Long version);
}
