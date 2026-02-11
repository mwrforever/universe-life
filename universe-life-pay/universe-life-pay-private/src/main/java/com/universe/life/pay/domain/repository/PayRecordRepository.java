package com.universe.life.pay.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.pay.infrastructure.persistence.po.PayRecordPO;
import com.universe.life.pay.model.dto.AdminPayRecordListQueryDTO;
import com.universe.life.pay.model.dto.UserPayRecordListQueryDTO;

import java.util.Optional;

public interface PayRecordRepository {

    PayRecordPO save(PayRecordPO po);

    Optional<PayRecordPO> findByRequestNo(String requestNo);

    boolean markSuccess(Long id, String thirdTradeNo, String extra);

    boolean markFail(Long id, String thirdTradeNo, String extra);

    IPage<PayRecordPO> pageAdminRecords(Page<PayRecordPO> page, AdminPayRecordListQueryDTO query);

    IPage<PayRecordPO> pageUserRecords(Page<PayRecordPO> page, Long payerId, UserPayRecordListQueryDTO query);

    Optional<PayRecordPO> findByRequestNoAndPayerId(String requestNo, Long payerId);
}
