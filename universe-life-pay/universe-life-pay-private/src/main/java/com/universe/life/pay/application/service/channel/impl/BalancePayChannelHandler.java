package com.universe.life.pay.application.service.channel.impl;

import com.universe.life.pay.application.service.BalanceApplicationService;
import com.universe.life.pay.application.service.channel.PayChannelHandler;
import com.universe.life.auth.common.domain.Result;
import com.universe.life.pay.infrastructure.persistence.po.PayRecordPO;
import com.universe.life.pay.model.enums.PayChannelEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BalancePayChannelHandler implements PayChannelHandler {

    private final BalanceApplicationService balanceApplicationService;

    @Override
    public int channelCode() {
        return PayChannelEnum.BALANCE.getCode();
    }

    @Override
    public ChannelCreateResult create(PayRecordPO record) {
        if (record == null) {
            throw new IllegalArgumentException("支付记录不能为空");
        }
        if (record.getRequestNo() == null || record.getRequestNo().isBlank()) {
            throw new IllegalArgumentException("requestNo不能为空");
        }
        if (record.getBizType() == null || record.getBizType().isBlank()) {
            throw new IllegalArgumentException("bizType不能为空");
        }
        if (record.getBizId() == null) {
            throw new IllegalArgumentException("bizId不能为空");
        }
        if (record.getPayerId() == null) {
            throw new IllegalArgumentException("payerId不能为空");
        }
        if (record.getAmount() == null || record.getAmount() <= 0) {
            throw new IllegalArgumentException("amount非法");
        }

        Result<String> freezeResult = balanceApplicationService.freeze(
                record.getRequestNo(),
                record.getBizType(),
                record.getBizId(),
                record.getPayerId(),
                record.getAmount(),
                null,
                record.getExtra()
        );

        if (freezeResult == null || freezeResult.code() == null || freezeResult.code() != 1) {
            throw new IllegalStateException(freezeResult == null ? "余额冻结失败" : freezeResult.message());
        }

        Result<Void> confirmResult = balanceApplicationService.confirmDebit(
                record.getRequestNo(),
                record.getBizType(),
                record.getBizId(),
                record.getPayerId(),
                record.getExtra()
        );

        if (confirmResult == null || confirmResult.code() == null || confirmResult.code() != 1) {
            throw new IllegalStateException(confirmResult == null ? "余额扣款确认失败" : confirmResult.message());
        }

        String thirdTradeNo = "BAL-" + record.getRequestNo();
        return new ChannelCreateResult(thirdTradeNo, null, null);
    }

    @Override
    public ChannelCallbackResult parseCallback(String body, java.util.Map<String, String> headers) {
        String requestNo = headers == null ? null : headers.get("requestNo");
        if (requestNo == null || requestNo.isBlank()) {
            requestNo = headers == null ? null : headers.get("request_no");
        }
        return new ChannelCallbackResult(requestNo, null, true, body);
    }
}
