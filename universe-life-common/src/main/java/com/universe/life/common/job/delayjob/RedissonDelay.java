package com.universe.life.common.job.delayjob;

import java.util.concurrent.TimeUnit;

/**
 * @author 毛伟然
 * @since 2025/11/6 10:09
 */
public interface RedissonDelay<T> {
    /**
     * 获取队列名称
     *
     * @return 队列名称
     */
    String getQueue();

    /**
     * 获取任务id
     *
     * @return 任务id
     */
    String getJobId();

    /**
     * 获取任务数据
     *
     * @return 任务数据
     */
    T getData();

    /**
     * 获取延时时间
     *
     * @return 延时时间
     */
    Long getDelayTime();

    /**
     * 获取时间单位
     *
     * @return 时间单位
     */
    TimeUnit getTimeUnit();


}
