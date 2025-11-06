package com.universe.life.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 毛伟然
 * @since 2025/11/6 10:22
 */
@Getter
@AllArgsConstructor
public enum RedissonDelayQueue {

    CLEAR_OLD_JWK_QUEUE("clear_old_jwk_queue");

    private final String value;

}
