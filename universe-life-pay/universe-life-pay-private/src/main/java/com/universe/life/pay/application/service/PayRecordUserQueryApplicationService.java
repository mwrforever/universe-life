package com.universe.life.pay.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.auth.common.domain.Result;
import com.universe.life.common.domain.PageResult;
import com.universe.life.pay.domain.repository.PayRecordRepository;
import com.universe.life.pay.infrastructure.persistence.po.PayRecordPO;
import com.universe.life.pay.interfaces.util.MaskUtil;
import com.universe.life.pay.model.dto.UserPayRecordDTO;
import com.universe.life.pay.model.dto.UserPayRecordListQueryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PayRecordUserQueryApplicationService {

    private final PayRecordRepository payRecordRepository;

    public Result<PageResult<UserPayRecordDTO>> pageMyRecords(Long payerId, UserPayRecordListQueryDTO query) {
        if (payerId == null) {
            return Result.error("未登录");
        }

        int pageNo = query == null || query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        int size = query == null || query.getSize() == null || query.getSize() <= 0 ? 20 : query.getSize();

        Page<PayRecordPO> page = new Page<>(pageNo, size);
        IPage<PayRecordPO> result = payRecordRepository.pageUserRecords(page, payerId, query);

        List<PayRecordPO> records = result == null ? null : result.getRecords();
        if (records == null || records.isEmpty()) {
            return Result.success(PageResult.empty(page));
        }

        List<UserPayRecordDTO> list = records.stream().map(this::toUserPayRecordDTO).toList();
        return Result.success(PageResult.of(list, result));
    }

    public Result<UserPayRecordDTO> getMyRecordByRequestNo(Long payerId, String requestNo) {
        if (payerId == null) {
            return Result.error("未登录");
        }
        if (requestNo == null || requestNo.isBlank()) {
            return Result.error("requestNo不能为空");
        }
        PayRecordPO record = payRecordRepository.findByRequestNoAndPayerId(requestNo, payerId).orElse(null);
        if (record == null) {
            return Result.error("支付记录不存在");
        }
        return Result.success(toUserPayRecordDTO(record));
    }

    private UserPayRecordDTO toUserPayRecordDTO(PayRecordPO po) {
        UserPayRecordDTO dto = new UserPayRecordDTO();
        dto.setId(po.getId());
        dto.setBizType(po.getBizType());
        dto.setBizId(po.getBizId());
        dto.setRequestNo(MaskUtil.mask(po.getRequestNo()));
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
