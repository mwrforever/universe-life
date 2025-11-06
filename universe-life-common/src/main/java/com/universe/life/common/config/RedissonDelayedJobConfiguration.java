package com.universe.life.common.config;

import com.universe.life.common.factory.RedissonDelayJobFactory;
import com.universe.life.common.job.delayjob.RedissonDelayJobQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

/**
 * @author 毛伟然
 * @since 2025/11/6 13:42
 */
@Configuration
public class RedissonDelayedJobConfiguration {

    @Bean
    public RedissonDelayJobQueue redissonDelayJobQueue(RedissonClient redissonClient) {
        return new RedissonDelayJobQueue(redissonClient);
    }

    @Bean
    public RedissonDelayJobFactory redissonDelayJobFactory(
            RedissonDelayJobQueue redissonDelayJobQueue,
            @Qualifier("redissonExecDelayedJobExecutor") Executor redissonExecDelayedJobExecutor,
            @Qualifier("redissonTakeDelayedJobExecutor") Executor redissonTakeDelayedJobExecutor
            ) {
        return new RedissonDelayJobFactory(redissonDelayJobQueue, redissonExecDelayedJobExecutor, redissonTakeDelayedJobExecutor);
    }

}
