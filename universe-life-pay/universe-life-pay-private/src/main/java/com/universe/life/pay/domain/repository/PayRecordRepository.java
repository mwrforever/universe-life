package com.universe.life.pay.domain.repository;

import com.universe.life.pay.infrastructure.persistence.po.PayRecordPO;

import java.util.Optional;

public interface PayRecordRepository {

    PayRecordPO save(PayRecordPO po);

    Optional<PayRecordPO> findByRequestNo(String requestNo);

    boolean markSuccess(Long id, String thirdTradeNo, String extra);

    boolean markFail(Long id, String thirdTradeNo, String extra);
}
