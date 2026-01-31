package com.universe.life.auth.common.constants;

/**
 * @author 毛伟然
 * @since 2025/11/11 14:00
 */
public interface RabbitMqConstants {

    interface Queue {
        // 用户认证相关信息的队列
        String AUTH_NOTIFY_BLACK_JWK_QUEUE = "auth.notify.black.jwk.queue";

        // 用户登录信息队列
        String USER_LOGIN_INFO_QUEUE = "user.login.info.queue";
    }

    interface Exchange {
        // 用户认证相关信息的交换机
        String AUTH_NOTIFY_JWK_EXCHANGE = "auth.notify.jwk.exchange";

        // 用户信息相关交换机
        String USER_NOTIFY_EXCHANGE = "user.notify.exchange";
    }

    interface Binding {
        // 用户认证相关信息的绑定
        String AUTH_NOTIFY_BLACK_JWK_BINDING = "auth.notify.black.jwk.binding";
    }

    interface RoutingKey {
        // 用户登录信息路由键
        String USER_LOGIN_INFO_ROUTING_KEY = "user.login.info";
    }

}
