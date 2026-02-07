package com.universe.life.pay.application.service.channel;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PayChannelHandlerRegistry {

    private final Map<Integer, PayChannelHandler> handlerMap;

    public PayChannelHandlerRegistry(List<PayChannelHandler> handlers) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(PayChannelHandler::channelCode, Function.identity(), (a, b) -> a));
    }

    public PayChannelHandler getRequired(Integer channel) {
        if (channel == null) {
            throw new IllegalArgumentException("channel不能为空");
        }
        PayChannelHandler handler = handlerMap.get(channel);
        if (handler == null) {
            throw new IllegalArgumentException("不支持的支付渠道: " + channel);
        }
        return handler;
    }
}
