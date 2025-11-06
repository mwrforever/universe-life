package com.universe.life.common.factory;

import cn.hutool.core.util.StrUtil;
import com.universe.life.common.job.delayjob.RedissonDelayJobQueue;
import com.universe.life.common.strategy.RedissonDelayStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * @author 毛伟然
 * @since 2025/11/6 10:34
 */
@Slf4j
@RequiredArgsConstructor
public class RedissonDelayJobFactory {

    private final RedissonDelayJobQueue redissonDelayJobQueue;

    private final Executor redissonTakeDelayedJobExecutor;
    private final Executor redissonExecDelayJobExecutor;

    private final Map<String, RedissonDelayStrategy> delayStrategyMap = new ConcurrentHashMap<>();

    public void register(List<RedissonDelayStrategy> delayStrategies) {
        delayStrategies.forEach(this::put);
    }

    public void put(RedissonDelayStrategy delayStrategy) {
        delayStrategyMap.put(delayStrategy.queue(), delayStrategy);
        redissonTakeDelayedJobExecutor.execute(new Consumer(delayStrategy.queue()));
    }

    @RequiredArgsConstructor
    private class Consumer implements Runnable {

        private final String queue;

        @Override
        public void run() {
            // 开始执行拉取任务
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 获取任务
                    String job = redissonDelayJobQueue.take(queue);
                    if (StrUtil.isBlank(job)) {
                        continue;
                    }
                    RedissonDelayStrategy delayStrategy = delayStrategyMap.get(queue);
                    if (delayStrategy == null) {
                        continue;
                    }
                    // 使用执行线程池执行任务
                    redissonExecDelayJobExecutor.execute(() -> delayStrategy.execute(job));
                } catch (InterruptedException e) {
                    log.error("线程{}提取延时任务失败：{}", Thread.currentThread().getName(), e.getMessage());
                }
            }
            log.info("线程{}被中断结束", Thread.currentThread().getName());
        }
    }

}
