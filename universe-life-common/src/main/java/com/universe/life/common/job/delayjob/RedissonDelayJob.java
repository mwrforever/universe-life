package com.universe.life.common.job.delayjob;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * @author 毛伟然
 * @since 2025/11/6 09:56
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedissonDelayJob<T> implements RedissonDelay<T> {

    /**
     * 任务id
     */
    private String jobId;

    /**
     * 队列名称
     */
    private String queue;

    /**
     * 任务数据
     */
    private T data;

    /**
     * 延时时间
     */
    private Long delayTime;

    /**
     * 时间单位
     */
    private TimeUnit timeUnit;

}
