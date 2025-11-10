package com.universe.life.common.util;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * RabbitMQ消息转换器
 * <p>
 * 基于Jackson实现的高性能消息转换器，支持对象、字符串、字节数组等多种类型的消息序列化和反序列化
 * 自动处理消息头信息，支持消息类型识别和版本控制
 *
 * @author universe-life
 * @version 1.0.0
 * @since 2024-11-10
 */
@Slf4j
@Component
public class RabbitMqMessageConverter implements MessageConverter {

    private final ObjectMapper objectMapper;

    /**
     * 消息类型头
     */
    private static final String MESSAGE_TYPE_HEADER = "__message_type__";

    /**
     * 消息版本头
     */
    private static final String MESSAGE_VERSION_HEADER = "__message_version__";

    /**
     * 默认消息版本
     */
    private static final String DEFAULT_VERSION = "1.0";

    public RabbitMqMessageConverter() {
        this.objectMapper = new ObjectMapper();
        // 配置ObjectMapper，可以根据需要添加更多配置
        this.objectMapper.findAndRegisterModules();
    }

    /**
     * 将Java对象转换为AMQP消息
     *
     * @param object      待转换的Java对象
     * @param messageProperties 消息属性
     * @return 转换后的AMQP消息
     * @throws MessageConversionException 转换异常
     */
    @Override
    public Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
        try {
            // 设置消息类型
            if (object != null) {
                messageProperties.setHeader(MESSAGE_TYPE_HEADER, object.getClass().getName());
                messageProperties.setHeader(MESSAGE_VERSION_HEADER, DEFAULT_VERSION);
                messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                messageProperties.setContentEncoding(StandardCharsets.UTF_8.name());
            }

            // 序列化对象
            byte[] body;
            if (object instanceof String) {
                body = ((String) object).getBytes(StandardCharsets.UTF_8);
            } else if (object instanceof byte[]) {
                body = (byte[]) object;
            } else {
                String json = objectMapper.writeValueAsString(object);
                body = json.getBytes(StandardCharsets.UTF_8);
            }

            log.debug("消息转换成功，类型: {}, 大小: {} bytes",
                    object != null ? object.getClass().getSimpleName() : "null",
                    body.length);

            return new Message(body, messageProperties);

        } catch (JsonProcessingException e) {
            log.error("消息序列化失败: {}", e.getMessage(), e);
            throw new MessageConversionException("消息序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将AMQP消息转换为Java对象
     *
     * @param message AMQP消息
     * @param targetClass 目标类型
     * @return 转换后的Java对象
     * @throws MessageConversionException 转换异常
     */
    @Override
    public Object fromMessage(Message message, Class<?> targetClass) throws MessageConversionException {
        if (message == null || message.getBody() == null) {
            return null;
        }

        try {
            byte[] body = message.getBody();
            MessageProperties properties = message.getMessageProperties();

            // 根据目标类型进行反序列化
            if (targetClass == String.class) {
                return new String(body, StandardCharsets.UTF_8);
            } else if (targetClass == byte[].class) {
                return body;
            } else if (targetClass == Object.class) {
                // 尝试获取消息类型信息
                String messageType = properties != null ?
                        properties.getHeader(MESSAGE_TYPE_HEADER) : null;

                if (StrUtil.isNotBlank(messageType)) {
                    try {
                        Class<?> actualType = Class.forName(messageType);
                        return objectMapper.readValue(body, actualType);
                    } catch (ClassNotFoundException e) {
                        log.warn("无法找到消息类型: {}, 使用Object类型", messageType);
                    }
                }

                return objectMapper.readValue(body, Object.class);
            } else {
                return objectMapper.readValue(body, targetClass);
            }

        } catch (JsonProcessingException e) {
            log.error("消息反序列化失败: {}", e.getMessage(), e);
            throw new MessageConversionException("消息反序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取消息类型
     *
     * @param message 消息
     * @return 消息类型，如果无法识别则返回null
     */
    public String getMessageType(Message message) {
        if (message == null || message.getMessageProperties() == null) {
            return null;
        }
        return message.getMessageProperties().getHeader(MESSAGE_TYPE_HEADER);
    }

    /**
     * 获取消息版本
     *
     * @param message 消息
     * @return 消息版本，如果没有设置则返回null
     */
    public String getMessageVersion(Message message) {
        if (message == null || message.getMessageProperties() == null) {
            return null;
        }
        return message.getMessageProperties().getHeader(MESSAGE_VERSION_HEADER);
    }

    /**
     * 检查消息版本是否兼容
     *
     * @param message        消息
     * @param requiredVersion 要求的版本
     * @return 是否兼容
     */
    public boolean isVersionCompatible(Message message, String requiredVersion) {
        String messageVersion = getMessageVersion(message);
        if (StrUtil.isBlank(messageVersion) || StrUtil.isBlank(requiredVersion)) {
            return true; // 如果没有版本信息，默认兼容
        }

        // 这里可以实现更复杂的版本比较逻辑
        // 目前简单实现：完全相同或消息版本较新
        return messageVersion.compareTo(requiredVersion) >= 0;
    }
}