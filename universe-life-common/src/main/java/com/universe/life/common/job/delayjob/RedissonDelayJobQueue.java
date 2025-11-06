package com.universe.life.common.job.delayjob;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;

/**
 * @author 毛伟然
 * @since 2025/11/6 10:39
 */
@Slf4j
@RequiredArgsConstructor
public class RedissonDelayJobQueue {

    private final RedissonClient redissonClient;

    /**
     * 添加任务
     *
     * @param job 任务
     */
    public <T> void offer(RedissonDelay<T> job) {
        // 从redis中获取队列
        RBlockingDeque<String> queue = redissonClient.getBlockingDeque(job.getQueue());
        // 获取延时队列
        RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(queue);
        // 将任务投放到延时队列中
        String jobStr = JSONUtil.toJsonStr(job);
        delayedQueue.offer(jobStr, job.getDelayTime(), job.getTimeUnit());
        log.info("添加任务成功：{}", jobStr);
    }

    /**
     * 获取任务
     *
     * @return 任务
     */
    public String take(String queue) throws InterruptedException {
        // 通过redisson获取队列任务
        RBlockingDeque<String> blockingDeque = redissonClient.getBlockingDeque(queue);
        if (blockingDeque == null) {
            return null;
        }
        return blockingDeque.take();
    }

}
