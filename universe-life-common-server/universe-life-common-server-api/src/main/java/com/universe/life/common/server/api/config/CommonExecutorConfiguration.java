package com.universe.life.common.server.api.config;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Slf4j
@Configuration
public class CommonExecutorConfiguration {

    private ThreadFactory getThreadFactory(String prefix) {
        return new ThreadFactoryBuilder()
                .setNamePrefix(prefix)
                .setUncaughtExceptionHandler((t, e) -> {
                    log.error("线程{} 出现异常：{}", t.getName(), e.getMessage());
                })
                .build();
    }

    @Bean
    public Executor captchaExecutor() {
        ThreadFactory threadFactory = getThreadFactory("captcha-");
        return new ThreadPoolExecutor(
                5,
                40,
                5,
                TimeUnit.MINUTES,
                new LinkedBlockingDeque<>(9999),
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

}
