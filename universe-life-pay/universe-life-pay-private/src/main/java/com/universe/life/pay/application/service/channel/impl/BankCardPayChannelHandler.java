package com.universe.life.pay.application.service.channel.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universe.life.pay.application.service.channel.PayChannelHandler;
import com.universe.life.pay.infrastructure.persistence.po.PayRecordPO;
import com.universe.life.pay.model.enums.PayChannelEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class BankCardPayChannelHandler implements PayChannelHandler {

    private final ObjectMapper objectMapper;

    @Override
    public int channelCode() {
        return PayChannelEnum.BANK_CARD.getCode();
    }

    @Override
    public ChannelCreateResult create(PayRecordPO record) {
        String thirdTradeNo = "BANK-" + record.getRequestNo();
        try {
            String payParams = objectMapper.writeValueAsString(Map.of(
                    "redirectUrl", "https://mock-bank-pay/redirect/" + record.getRequestNo(),
                    "tradeNo", thirdTradeNo
            ));
            return new ChannelCreateResult(thirdTradeNo, null, payParams);
        } catch (Exception e) {
            return new ChannelCreateResult(thirdTradeNo, null, null);
        }
    }

    @Override
    public ChannelCallbackResult parseCallback(String body, Map<String, String> headers) {
        return PayCallbackParser.parse(body, objectMapper);
    }
}
