package com.universe.life.auth.service.config;

import com.universe.life.common.factory.RedissonDelayJobFactory;
import com.universe.life.common.job.delayjob.RedissonDelayJobQueue;
import com.universe.life.common.strategy.RedissonDelayStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author 毛伟然
 * @since 2025/11/7 09:32
 */
@Configuration
public class RedissonDelayedJobConfiguration {

    @Bean
    @ConditionalOnMissingBean(RedissonDelayJobFactory.class)
    public RedissonDelayJobFactory redissonDelayJobFactory(
            RedissonDelayJobQueue redissonDelayJobQueue,
            @Qualifier("redissonExecDelayedJobExecutor") Executor redissonExecDelayedJobExecutor,
            @Qualifier("redissonTakeDelayedJobExecutor") Executor redissonTakeDelayedJobExecutor,
            List<RedissonDelayStrategy> jobs
    ) {
        RedissonDelayJobFactory redissonDelayJobFactory = new RedissonDelayJobFactory(redissonDelayJobQueue, redissonExecDelayedJobExecutor, redissonTakeDelayedJobExecutor);
        redissonDelayJobFactory.register(jobs);
        return redissonDelayJobFactory;
    }

}
