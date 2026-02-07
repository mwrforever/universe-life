package com.universe.life.pay.application.service.channel.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.universe.life.pay.application.service.channel.PayChannelHandler;

public class PayCallbackParser {

    private PayCallbackParser() {
    }

    public static PayChannelHandler.ChannelCallbackResult parse(String body, ObjectMapper objectMapper) {
        if (body == null || body.isBlank()) {
            return new PayChannelHandler.ChannelCallbackResult(null, null, false, body);
        }

        PayChannelHandler.ChannelCallbackResult jsonParsed = tryParseJson(body, objectMapper);
        if (jsonParsed != null) {
            return jsonParsed;
        }

        String requestNo = extract(body, "requestNo");
        String thirdTradeNo = extract(body, "thirdTradeNo");
        String successStr = extract(body, "success");
        boolean success = "true".equalsIgnoreCase(successStr) || "1".equals(successStr);

        return new PayChannelHandler.ChannelCallbackResult(requestNo, thirdTradeNo, success, body);
    }

    private static PayChannelHandler.ChannelCallbackResult tryParseJson(String body, ObjectMapper objectMapper) {
        if (objectMapper == null) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node == null || node.isNull()) {
                return null;
            }
            String requestNo = firstText(node,
                    "requestNo",
                    "request_no",
                    "outTradeNo",
                    "out_trade_no",
                    "merchantOrderNo",
                    "merchant_order_no"
            );
            String thirdTradeNo = firstText(node,
                    "thirdTradeNo",
                    "third_trade_no",
                    "tradeNo",
                    "trade_no",
                    "transactionId",
                    "transaction_id"
            );
            Boolean success = firstBool(node,
                    "success",
                    "paid",
                    "paySuccess",
                    "pay_success"
            );

            if (requestNo == null && thirdTradeNo == null && success == null) {
                return null;
            }

            return new PayChannelHandler.ChannelCallbackResult(requestNo, thirdTradeNo, success != null && success, body);
        } catch (Exception e) {
            return null;
        }
    }

    private static String firstText(JsonNode node, String... fields) {
        if (fields == null) {
            return null;
        }
        for (String field : fields) {
            String v = text(node, field);
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }

    private static Boolean firstBool(JsonNode node, String... fields) {
        if (fields == null) {
            return null;
        }
        for (String field : fields) {
            Boolean v = bool(node, field);
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    private static String text(JsonNode node, String field) {
        if (node == null || field == null) {
            return null;
        }
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            return null;
        }
        return v.asText(null);
    }

    private static Boolean bool(JsonNode node, String field) {
        if (node == null || field == null) {
            return null;
        }
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            return null;
        }
        if (v.isBoolean()) {
            return v.asBoolean();
        }
        String s = v.asText(null);
        if (s == null) {
            return null;
        }
        if ("true".equalsIgnoreCase(s) || "1".equals(s)) {
            return true;
        }
        if ("false".equalsIgnoreCase(s) || "0".equals(s)) {
            return false;
        }
        return null;
    }

    private static String extract(String body, String key) {
        int i = body.indexOf('"' + key + '"');
        if (i < 0) {
            return null;
        }
        int colon = body.indexOf(':', i);
        if (colon < 0) {
            return null;
        }
        int start = body.indexOf('"', colon + 1);
        if (start < 0) {
            int valueStart = colon + 1;
            int end = body.indexOf(',', valueStart);
            if (end < 0) {
                end = body.indexOf('}', valueStart);
            }
            if (end < 0) {
                end = body.length();
            }
            return body.substring(valueStart, end).trim().replace("\"", "");
        }
        int end = body.indexOf('"', start + 1);
        if (end < 0) {
            return null;
        }
        return body.substring(start + 1, end);
    }
}
