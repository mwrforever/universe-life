package com.universe.life.pay.application.service.channel.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import com.universe.life.pay.application.service.channel.PayChannelHandler;
import com.universe.life.pay.infrastructure.persistence.po.PayRecordPO;
import com.universe.life.pay.infrastructure.wechat.WechatPayProperties;
import com.universe.life.pay.model.enums.PayChannelEnum;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class WechatPayChannelHandler implements PayChannelHandler {

    private final ObjectMapper objectMapper;

    private final WechatPayProperties properties;

    private final CloseableHttpClient wechatPayHttpClient;

    private final Verifier wechatPayVerifier;

    private final PrivateKey wechatPayMerchantPrivateKey;

    public WechatPayChannelHandler(ObjectMapper objectMapper,
                                  WechatPayProperties properties,
                                  @Nullable CloseableHttpClient wechatPayHttpClient,
                                  @Nullable Verifier wechatPayVerifier,
                                  @Nullable PrivateKey wechatPayMerchantPrivateKey) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.wechatPayHttpClient = wechatPayHttpClient;
        this.wechatPayVerifier = wechatPayVerifier;
        this.wechatPayMerchantPrivateKey = wechatPayMerchantPrivateKey;
    }

    @Override
    public int channelCode() {
        return PayChannelEnum.WECHAT.getCode();
    }

    @Override
    public ChannelCreateResult create(PayRecordPO record) {
        if (!properties.isEnabled()) {
            String prepayId = "WX-PREPAY-" + (record.getId() == null ? record.getRequestNo() : record.getId());
            try {
                String payParams = objectMapper.writeValueAsString(Map.of(
                        "appId", "mock",
                        "nonceStr", record.getRequestNo(),
                        "package", "prepay_id=" + prepayId
                ));
                return new ChannelCreateResult(null, prepayId, payParams);
            } catch (Exception e) {
                return new ChannelCreateResult(null, prepayId, null);
            }
        }

        ensureWechatEnabledDeps();

        if (record.getRequestNo() == null || record.getRequestNo().isBlank()) {
            throw new IllegalArgumentException("requestNo不能为空");
        }
        if (record.getId() == null) {
            throw new IllegalArgumentException("payRecordId不能为空（请先落库生成ID再发起微信下单）");
        }
        if (String.valueOf(record.getId()).length() > 32) {
            throw new IllegalArgumentException("payRecordId过长，无法作为微信 out_trade_no");
        }

        String prepayId;
        if (record.getThirdPrepayId() != null && !record.getThirdPrepayId().isBlank()) {
            prepayId = record.getThirdPrepayId();
        } else {
            String openid = extractOpenid(record.getExtra());
            if (openid == null || openid.isBlank()) {
                throw new IllegalArgumentException("微信支付JSAPI需要openid，请在extra中提供JSON: {\"openid\":\"...\"}");
            }
            prepayId = jsapiPrepay(record, openid);
        }

        long timeStamp = Instant.now().getEpochSecond();
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        String pkg = "prepay_id=" + prepayId;
        String paySign = signJsapi(properties.getAppId(), String.valueOf(timeStamp), nonceStr, pkg);

        try {
            String payParams = objectMapper.writeValueAsString(Map.of(
                    "appId", properties.getAppId(),
                    "timeStamp", String.valueOf(timeStamp),
                    "nonceStr", nonceStr,
                    "package", pkg,
                    "signType", "RSA",
                    "paySign", paySign
            ));
            return new ChannelCreateResult(null, prepayId, payParams);
        } catch (Exception e) {
            return new ChannelCreateResult(null, prepayId, null);
        }
    }

    @Override
    public boolean verifyCallback(String body, Map<String, String> headers) {
        if (!properties.isEnabled()) {
            return true;
        }
        ensureWechatEnabledDeps();
        String timestamp = headerIgnoreCase(headers, "Wechatpay-Timestamp");
        String nonce = headerIgnoreCase(headers, "Wechatpay-Nonce");
        String signature = headerIgnoreCase(headers, "Wechatpay-Signature");
        String serial = headerIgnoreCase(headers, "Wechatpay-Serial");
        if (timestamp == null || nonce == null || signature == null || serial == null) {
            return false;
        }
        String message = timestamp + "\n" + nonce + "\n" + (body == null ? "" : body) + "\n";
        return wechatPayVerifier.verify(serial, message.getBytes(StandardCharsets.UTF_8), signature);
    }

    @Override
    public ChannelCallbackResult parseCallback(String body, Map<String, String> headers) {
        if (!properties.isEnabled()) {
            return PayCallbackParser.parse(body, objectMapper);
        }
        ensureWechatEnabledDeps();

        try {
            ObjectNode root = (ObjectNode) objectMapper.readTree(body);
            ObjectNode resource = (ObjectNode) root.get("resource");
            if (resource == null) {
                return PayCallbackParser.parse(body, objectMapper);
            }

            String associatedData = resource.path("associated_data").asText(null);
            String nonce = resource.path("nonce").asText(null);
            String ciphertext = resource.path("ciphertext").asText(null);
            if (nonce == null || ciphertext == null) {
                return PayCallbackParser.parse(body, objectMapper);
            }

            AesUtil aesUtil = new AesUtil(properties.getApiV3Key().getBytes(StandardCharsets.UTF_8));
            String plaintext = aesUtil.decryptToString(
                    associatedData == null ? null : associatedData.getBytes(StandardCharsets.UTF_8),
                    nonce.getBytes(StandardCharsets.UTF_8),
                    ciphertext
            );

            ObjectNode payResult = (ObjectNode) objectMapper.readTree(plaintext);
            String outTradeNo = payResult.path("out_trade_no").asText(null);
            String attach = payResult.path("attach").asText(null);
            String transactionId = payResult.path("transaction_id").asText(null);
            String tradeState = payResult.path("trade_state").asText(null);
            boolean success = "SUCCESS".equalsIgnoreCase(tradeState);

            String requestNo = (attach == null || attach.isBlank()) ? outTradeNo : attach;
            return new ChannelCallbackResult(requestNo, transactionId, success, plaintext);
        } catch (Exception e) {
            return PayCallbackParser.parse(body, objectMapper);
        }
    }

    private void ensureWechatEnabledDeps() {
        if (wechatPayHttpClient == null || wechatPayVerifier == null || wechatPayMerchantPrivateKey == null) {
            throw new IllegalStateException("微信支付已启用但SDK依赖未就绪，请检查pay.wechat.*配置与证书私钥加载");
        }
    }

    private String extractOpenid(String extra) {
        if (extra == null || extra.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(extra).path("openid").asText(null);
        } catch (Exception e) {
            return null;
        }
    }

    private String jsapiPrepay(PayRecordPO record, String openid) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("appid", properties.getAppId());
            body.put("mchid", properties.getMchId());
            body.put("description", record.getBizType() + "-" + record.getBizId());
            body.put("out_trade_no", String.valueOf(record.getId()));
            body.put("attach", record.getRequestNo());
            body.put("notify_url", properties.getNotifyUrl());

            ObjectNode amount = body.putObject("amount");
            amount.put("total", Math.toIntExact(record.getAmount()));
            amount.put("currency", "CNY");

            ObjectNode payer = body.putObject("payer");
            payer.put("openid", openid);

            HttpPost post = new HttpPost("https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi");
            post.setEntity(new StringEntity(objectMapper.writeValueAsString(body), ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = wechatPayHttpClient.execute(post)) {
                int code = response.getStatusLine().getStatusCode();
                String resp = response.getEntity() == null ? null : EntityUtils.toString(response.getEntity());
                if (code < 200 || code >= 300) {
                    throw new IllegalStateException("微信下单失败: http=" + code + ", resp=" + resp);
                }
                ObjectNode node = (ObjectNode) objectMapper.readTree(resp);
                String prepayId = node.path("prepay_id").asText(null);
                if (prepayId == null || prepayId.isBlank()) {
                    throw new IllegalStateException("微信下单返回缺少prepay_id: " + resp);
                }
                return prepayId;
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("微信下单异常", e);
        }
    }

    private String signJsapi(String appId, String timeStamp, String nonceStr, String pkg) {
        try {
            String message = appId + "\n" + timeStamp + "\n" + nonceStr + "\n" + pkg + "\n";
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(wechatPayMerchantPrivateKey);
            signature.update(message.getBytes(StandardCharsets.UTF_8));
            byte[] sign = signature.sign();
            return java.util.Base64.getEncoder().encodeToString(sign);
        } catch (Exception e) {
            throw new IllegalStateException("生成微信JSAPI支付签名失败", e);
        }
    }

    private String headerIgnoreCase(Map<String, String> headers, String key) {
        if (headers == null || key == null) {
            return null;
        }
        for (Map.Entry<String, String> e : headers.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(key)) {
                return e.getValue();
            }
        }
        return null;
    }
}
