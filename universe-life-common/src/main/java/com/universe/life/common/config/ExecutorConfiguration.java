package com.universe.life.common.config;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @author 毛伟然
 * @since 2025/11/6 14:53
 */
@Slf4j
@Configuration
public class ExecutorConfiguration {

    @Bean
    public Executor redissonTakeDelayedJobExecutor() {
        ThreadFactory factory = new ThreadFactoryBuilder()
                .setNamePrefix("redisson-take-delayed-job-")
                .setUncaughtExceptionHandler((t, e) -> {
                    log.error("线程{} 获取redisson延时任务出现异常：{}", t.getName(), e.getMessage());
                    try {
                        Thread.sleep(200_000);
                    } catch (InterruptedException ex) {
                    }
                })
                .build();
        return new ThreadPoolExecutor(
                2,
                2,
                2L,
                TimeUnit.MINUTES,
                new SynchronousQueue<>(),
                factory,
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    @Bean
    public Executor redissonExecDelayedJobExecutor() {
        ThreadFactory factory = new ThreadFactoryBuilder()
                .setNamePrefix("redisson-exec-delayed-job-")
                .setUncaughtExceptionHandler((t, e) -> {
                    log.error("线程{} 执行redisson延时任务出现异常：{}", t.getName(), e.getMessage());
                    try {
                        Thread.sleep(200_000);
                    } catch (InterruptedException ex) {
                    }
                })
                .build();
        return new ThreadPoolExecutor(
                8,
                16,
                2L,
                TimeUnit.MINUTES,
                new LinkedBlockingDeque<>(5000),
                factory,
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

}
