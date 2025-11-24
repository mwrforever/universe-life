package com.universe.life.auth.service.constants;

import java.time.Duration;

/**
 * @author 毛伟然
 * @since 2025/10/30 15:29
 */
public interface JwtConstants {

    String USER_TOKEN = "user_token";

    String TOKEN_JTI = "token_jti";

    Duration USER_TOKEN_EXPIRE = Duration.ofMillis(5);

    Duration REFRESH_TOKEN_EXPIRE = Duration.ofHours(2);

    Duration REMEMBER_REFRESH_TOKEN_EXPIRE = Duration.ofDays(7);

    String CLIENT_ID = "client_id";

    String USER_ID = "user_id";
}
