package com.universe.life.common.config;

import com.universe.life.common.util.RabbitMqSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

/**
 * @author 毛伟然
 * @since 2025/11/11 11:22
 */
@Slf4j
@Configuration
public class RabbitMqConfiguration {

    @Bean
    public MessageConverter rabbitMqMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory factory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(factory);
        rabbitTemplate.setMessageConverter(rabbitMqMessageConverter());
        rabbitTemplate.setReturnsCallback(returned ->
            log.warn("消息被退回: {} -> {}, 原因: {}",
                    returned.getExchange(),
                    returned.getRoutingKey(),
                    returned.getReplyText())
        );
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            String msgId = correlationData == null ? "null" : correlationData.getId();
            if (ack) {
                log.info("消息已送达 Exchange: {}", msgId);
            } else {
                log.error("消息未送达 Exchange: {}, 原因: {}", msgId, cause);
            }
        });
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(rabbitMqMessageConverter());
        return factory;
    }

    @Bean
    @ConditionalOnBean(RabbitTemplate.class)
    public RabbitMqSender rabbitMqSender(
            RabbitTemplate rabbitTemplate,
            @Qualifier("rabbitMqSendMessageExecutor") Executor executor
    ) {
        return new RabbitMqSender(rabbitTemplate, executor);
    }

}
