package com.universe.life.pay.domain.repository;

import com.universe.life.pay.infrastructure.persistence.po.PayBalanceFreezePO;

import java.util.Optional;

public interface PayBalanceFreezeRepository {

    PayBalanceFreezePO save(PayBalanceFreezePO po);

    Optional<PayBalanceFreezePO> findByFreezeNo(String freezeNo);

    Optional<PayBalanceFreezePO> findByRequestNoAndPayerId(String requestNo, Long payerId);

    Optional<PayBalanceFreezePO> findByBiz(String bizType, Long bizId, Long payerId);

    boolean markConfirmedDebit(Long id);

    boolean markUnfrozen(Long id);

    boolean markClosed(Long id);
}
