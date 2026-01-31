package com.universe.life.auth.gateway.listening;

import com.universe.life.auth.gateway.manager.JwtDecoderManager;
import com.universe.life.auth.common.constants.RabbitMqConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author 毛伟然
 * @since 2025/11/11 22:26
 */
@Component
@RequiredArgsConstructor
public class JwkNotifyListener {


    private final JwtDecoderManager jwtDecoderManager;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    name = RabbitMqConstants.Queue.AUTH_NOTIFY_BLACK_JWK_QUEUE,
                    durable = "true",
                    exclusive = "false",
                    autoDelete = "false"
            ),
            exchange = @Exchange(
                    name = RabbitMqConstants.Exchange.AUTH_NOTIFY_JWK_EXCHANGE,
                    type = "topic",
                    durable = "true",
                    autoDelete = "false"
            ),
            key = RabbitMqConstants.Binding.AUTH_NOTIFY_BLACK_JWK_BINDING
    ))
    public void blackJwkNotify(String message) {
        jwtDecoderManager.resetDecoder();
    }
}
