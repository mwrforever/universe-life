package com.universe.life.auth.common.constants;

/**
 * @author 毛伟然
 * @since 2025/11/4 09:55
 */
public interface RedisConstants {

    String USER_AUTH_UID_KEY = "user:auth:uid:";

    String REDISSON_DELAYED_QUEUE_JWK = "redisson:delayed:queue:jwk";

    String AUTH_SECRET_KEY_GENERATE_LOCK = "auth:secret:key:generate:lock";

    String AUTH_USER_CAPTCHA_KEY_PREFIX = "auth:user:captcha:";

    String AUTH_USER_CAPTCHA_LOCK = "auth:user:captcha:lock:";

    String AUTH_ISSUER = "issuer";

    String AUTH_IDENTIFICATION = "identification";

    int AUTH_USER_CAPTCHA_EXPIRE_TIME = 30;

    String AUTH_USER_DATA = "data";
    String AUTH_USER_TYPE = "type";
}
