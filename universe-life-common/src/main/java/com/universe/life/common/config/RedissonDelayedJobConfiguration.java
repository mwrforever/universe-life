package com.universe.life.common.config;

import com.universe.life.common.job.delayjob.RedissonDelayJobQueue;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 毛伟然
 * @since 2025/11/6 13:42
 */
@Configuration
@ConditionalOnProperty(name = "universe-life.redisson.enabled", havingValue = "true", matchIfMissing = true)
public class RedissonDelayedJobConfiguration {

    @Bean
    public RedissonDelayJobQueue redissonDelayJobQueue(RedissonClient redissonClient) {
        return new RedissonDelayJobQueue(redissonClient);
    }

}
