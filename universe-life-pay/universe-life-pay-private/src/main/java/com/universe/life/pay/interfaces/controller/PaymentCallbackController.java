package com.universe.life.pay.interfaces.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.pay.application.service.PayApplicationService;
import com.universe.life.pay.application.service.channel.PayChannelHandler;
import com.universe.life.pay.application.service.channel.PayChannelHandlerRegistry;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "支付回调", description = "第三方支付回调入口")
@RestController
@RequestMapping("/callbacks")
@RequiredArgsConstructor
public class PaymentCallbackController {

    private final PayChannelHandlerRegistry handlerRegistry;
    private final PayApplicationService payApplicationService;

    @PostMapping("/{channel}")
    public Result<Void> callback(
            @PathVariable Integer channel,
            @RequestBody(required = false) String body,
            @RequestParam(required = false) Map<String, String> params,
            @RequestHeader HttpHeaders headers
    ) {
        Map<String, String> headerMap = new HashMap<>();
        headers.forEach((k, v) -> headerMap.put(k, v == null || v.isEmpty() ? null : v.get(0)));

        String rawBody = (params != null && !params.isEmpty()) ? buildFormBody(params) : body;

        PayChannelHandler handler = handlerRegistry.getRequired(channel);

        Map<String, String> verifyHeaders = (params != null && !params.isEmpty()) ? params : headerMap;

        if (!handler.verifyCallback(rawBody, verifyHeaders)) {
            return Result.error("回调校验失败");
        }

        PayChannelHandler.ChannelCallbackResult parsed = handler.parseCallback(rawBody, verifyHeaders);

        return payApplicationService.handleCallback(
                channel,
                parsed == null ? rawBody : parsed.raw(),
                parsed == null ? null : parsed.requestNo(),
                parsed == null ? null : parsed.thirdTradeNo(),
                parsed != null && parsed.success()
        );
    }

    private String buildFormBody(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!sb.isEmpty()) {
                sb.append('&');
            }
            sb.append(e.getKey()).append('=');
            if (e.getValue() != null) {
                sb.append(e.getValue());
            }
        }
        return sb.toString();
    }
}
