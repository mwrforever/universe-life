package com.universe.life.pay.application.service.channel.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.internal.util.AlipaySignature;
import com.universe.life.pay.application.service.channel.PayChannelHandler;
import com.universe.life.pay.infrastructure.alipay.AlipayProperties;
import com.universe.life.pay.infrastructure.persistence.po.PayRecordPO;
import com.universe.life.pay.model.enums.PayChannelEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AlipayPayChannelHandler implements PayChannelHandler {

    private final ObjectMapper objectMapper;

    private final AlipayProperties properties;

    private final AlipayClient alipayClient;

    @Override
    public int channelCode() {
        return PayChannelEnum.ALIPAY.getCode();
    }

    @Override
    public ChannelCreateResult create(PayRecordPO record) {
        if (!properties.isEnabled()) {
            String thirdTradeNo = "ALI-" + record.getRequestNo();
            try {
                String payParams = objectMapper.writeValueAsString(Map.of(
                        "orderString", "mock-order-string-" + record.getRequestNo(),
                        "tradeNo", thirdTradeNo
                ));
                return new ChannelCreateResult(thirdTradeNo, null, payParams);
            } catch (Exception e) {
                return new ChannelCreateResult(thirdTradeNo, null, null);
            }
        }

        if (alipayClient == null) {
            throw new IllegalStateException("支付宝已启用但SDK依赖未就绪，请检查pay.alipay.*配置");
        }

        try {
            AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
            model.setOutTradeNo(record.getRequestNo());
            model.setSubject(record.getBizType() + "-" + record.getBizId());
            model.setTotalAmount(String.format("%.2f", record.getAmount() / 100.0));
            model.setProductCode("QUICK_MSECURITY_PAY");

            AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
            request.setBizModel(model);
            request.setNotifyUrl(properties.getNotifyUrl());

            String orderString = alipayClient.sdkExecute(request).getBody();
            return new ChannelCreateResult(null, null, orderString);
        } catch (AlipayApiException e) {
            throw new IllegalStateException("支付宝下单失败", e);
        }
    }

    @Override
    public boolean verifyCallback(String body, Map<String, String> headers) {
        if (!properties.isEnabled()) {
            return true;
        }
        try {
            return AlipaySignature.rsaCheckV1(
                    headers,
                    properties.getAlipayPublicKey(),
                    properties.getCharset(),
                    properties.getSignType()
            );
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ChannelCallbackResult parseCallback(String body, Map<String, String> headers) {
        if (!properties.isEnabled()) {
            return PayCallbackParser.parse(body, objectMapper);
        }
        String requestNo = firstNonBlank(headers.get("out_trade_no"), headers.get("outTradeNo"));
        String tradeNo = firstNonBlank(headers.get("trade_no"), headers.get("tradeNo"));
        String tradeStatus = firstNonBlank(headers.get("trade_status"), headers.get("tradeStatus"));
        boolean success = "TRADE_SUCCESS".equalsIgnoreCase(tradeStatus) || "TRADE_FINISHED".equalsIgnoreCase(tradeStatus);
        return new ChannelCallbackResult(requestNo, tradeNo, success, body);
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }
}
