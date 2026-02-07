package com.universe.life.pay.application.service.channel;

import com.universe.life.pay.infrastructure.persistence.po.PayRecordPO;

import java.util.Map;

public interface PayChannelHandler {

    int channelCode();

    ChannelCreateResult create(PayRecordPO record);

    default boolean verifyCallback(String body, Map<String, String> headers) {
        return true;
    }

    ChannelCallbackResult parseCallback(String body, Map<String, String> headers);

    record ChannelCreateResult(String thirdTradeNo, String thirdPrepayId, String payParams) {
    }

    record ChannelCallbackResult(String requestNo, String thirdTradeNo, boolean success, String raw) {
    }
}
