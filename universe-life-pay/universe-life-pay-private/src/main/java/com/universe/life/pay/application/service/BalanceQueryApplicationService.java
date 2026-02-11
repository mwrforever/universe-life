package com.universe.life.pay.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.auth.common.domain.Result;
import com.universe.life.common.domain.PageResult;
import com.universe.life.pay.domain.repository.PayBalanceAccountRepository;
import com.universe.life.pay.domain.repository.PayBalanceFlowRepository;
import com.universe.life.pay.infrastructure.persistence.po.PayBalanceAccountPO;
import com.universe.life.pay.infrastructure.persistence.po.PayBalanceFlowPO;
import com.universe.life.pay.interfaces.util.MaskUtil;
import com.universe.life.pay.model.dto.AdminBalanceFlowDTO;
import com.universe.life.pay.model.dto.AdminBalanceFlowListQueryDTO;
import com.universe.life.pay.model.dto.BalanceAccountDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BalanceQueryApplicationService {

    private final PayBalanceAccountRepository balanceAccountRepository;
    private final PayBalanceFlowRepository balanceFlowRepository;

    public Result<BalanceAccountDTO> queryMyBalance(Long userId) {
        if (userId == null) {
            return Result.error("未登录");
        }
        PayBalanceAccountPO account = balanceAccountRepository.findByUserId(userId).orElse(null);
        if (account == null) {
            return Result.error("账户不存在");
        }
        BalanceAccountDTO dto = new BalanceAccountDTO();
        dto.setCurrency(account.getCurrency());
        dto.setAvailableBalance(account.getAvailableBalance());
        dto.setFrozenBalance(account.getFrozenBalance());
        dto.setUpdatedAt(account.getUpdatedAt());
        return Result.success(dto);
    }

    public Result<PageResult<AdminBalanceFlowDTO>> pageAdminFlows(AdminBalanceFlowListQueryDTO query) {
        int pageNo = query == null || query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        int size = query == null || query.getSize() == null || query.getSize() <= 0 ? 20 : query.getSize();

        Page<PayBalanceFlowPO> page = new Page<>(pageNo, size);
        IPage<PayBalanceFlowPO> result = balanceFlowRepository.pageAdminFlows(page, query);

        List<PayBalanceFlowPO> records = result == null ? null : result.getRecords();
        if (records == null || records.isEmpty()) {
            return Result.success(PageResult.empty(page));
        }

        List<AdminBalanceFlowDTO> list = records.stream().map(this::toAdminFlowDTO).toList();
        return Result.success(PageResult.of(list, result));
    }

    private AdminBalanceFlowDTO toAdminFlowDTO(PayBalanceFlowPO po) {
        AdminBalanceFlowDTO dto = new AdminBalanceFlowDTO();
        dto.setFlowNo(po.getFlowNo());
        dto.setRequestNo(MaskUtil.mask(po.getRequestNo()));
        dto.setUserId(po.getUserId());
        dto.setBizType(po.getBizType());
        dto.setBizId(po.getBizId());
        dto.setAction(po.getAction());
        dto.setAmount(po.getAmount());
        dto.setAvailableDelta(po.getAvailableDelta());
        dto.setFrozenDelta(po.getFrozenDelta());
        dto.setBalanceAfter(po.getBalanceAfter());
        dto.setFrozenAfter(po.getFrozenAfter());
        dto.setCreatedAt(po.getCreatedAt());
        return dto;
    }
}
