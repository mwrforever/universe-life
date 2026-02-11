package com.universe.life.pay.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.pay.infrastructure.persistence.po.PayBalanceFlowPO;
import com.universe.life.pay.model.dto.AdminBalanceFlowListQueryDTO;

public interface PayBalanceFlowRepository {

    PayBalanceFlowPO save(PayBalanceFlowPO po);

    boolean existsByRequestNoUserAction(String requestNo, Long userId, String action);

    IPage<PayBalanceFlowPO> pageAdminFlows(Page<PayBalanceFlowPO> page, AdminBalanceFlowListQueryDTO query);
}
