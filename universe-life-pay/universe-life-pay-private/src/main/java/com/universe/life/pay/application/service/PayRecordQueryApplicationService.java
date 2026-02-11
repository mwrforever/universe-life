package com.universe.life.pay.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.auth.common.domain.Result;
import com.universe.life.common.domain.PageResult;
import com.universe.life.pay.domain.repository.PayRecordRepository;
import com.universe.life.pay.infrastructure.persistence.po.PayRecordPO;
import com.universe.life.pay.interfaces.util.MaskUtil;
import com.universe.life.pay.model.dto.AdminPayRecordDTO;
import com.universe.life.pay.model.dto.AdminPayRecordListQueryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PayRecordQueryApplicationService {

    private final PayRecordRepository payRecordRepository;

    public Result<PageResult<AdminPayRecordDTO>> pageAdminRecords(AdminPayRecordListQueryDTO query) {
        int pageNo = query == null || query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        int size = query == null || query.getSize() == null || query.getSize() <= 0 ? 20 : query.getSize();

        Page<PayRecordPO> page = new Page<>(pageNo, size);
        IPage<PayRecordPO> result = payRecordRepository.pageAdminRecords(page, query);

        List<PayRecordPO> records = result == null ? null : result.getRecords();
        if (records == null || records.isEmpty()) {
            return Result.success(PageResult.empty(page));
        }

        List<AdminPayRecordDTO> list = records.stream().map(this::toAdminPayRecordDTO).toList();
        return Result.success(PageResult.of(list, result));
    }

    private AdminPayRecordDTO toAdminPayRecordDTO(PayRecordPO po) {
        AdminPayRecordDTO dto = new AdminPayRecordDTO();
        dto.setId(po.getId());
        dto.setBizType(po.getBizType());
        dto.setBizId(po.getBizId());
        dto.setRequestNo(MaskUtil.mask(po.getRequestNo()));
        dto.setPayerId(po.getPayerId());
        dto.setPayeeId(po.getPayeeId());
        dto.setAmount(po.getAmount());
        dto.setChannel(po.getChannel());
        dto.setStatus(po.getStatus());
        dto.setThirdTradeNo(MaskUtil.mask(po.getThirdTradeNo()));
        dto.setThirdPrepayId(po.getThirdPrepayId());
        dto.setPaidAt(po.getPaidAt());
        dto.setCreatedAt(po.getCreatedAt());
        return dto;
    }
}
