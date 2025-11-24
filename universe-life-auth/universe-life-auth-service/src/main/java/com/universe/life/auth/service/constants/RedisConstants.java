package com.universe.life.auth.service.constants;

/**
 * @author 毛伟然
 * @since 2025/10/30 14:28
 */
public interface RedisConstants {

    String REDISSON_DELAYED_QUEUE_JWK = "redisson:delayed:queue:jwk";

    String AUTH_SECRET_KEY_GENERATE_LOCK = "auth:secret:key:generate:lock";

    String AUTH_USER_CAPTCHA_KEY_PREFIX = "auth:user:captcha:key:";

    String AUTH_USER_CAPTCHA_LOCK = "auth:user:captcha:lock:";

    String AUTH_ISSUER = "issuer";

    String AUTH_IDENTIFICATION = "identification";

    String USER_NORMAL_STATUS_KEY = "user:normal:status:key";

    String REDISSON_USER_WHITELIST_LOCK = "redisson:user:whitelist:lock";

}
