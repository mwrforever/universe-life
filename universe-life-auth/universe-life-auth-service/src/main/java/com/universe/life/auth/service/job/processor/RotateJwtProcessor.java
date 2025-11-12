package com.universe.life.auth.service.job.processor;

import com.universe.life.auth.service.constants.RedisConstants;
import com.universe.life.auth.service.manager.JwkManager;
import com.universe.life.common.job.delayjob.RedissonDelayJob;
import com.universe.life.common.job.delayjob.RedissonDelayJobQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author 毛伟然
 * @since 2025/11/6 09:17
 */
@Component
@RequiredArgsConstructor
public class RotateJwtProcessor implements BasicProcessor {

    private final JwkManager jwkManager;
    private final RedissonDelayJobQueue redissonDelayJobQueue;

    @Override
    public ProcessResult process(TaskContext taskContext) throws Exception {
        // 执行轮换任务
        jwkManager.rotate();
        // 生成清理jwk延时任务
        RedissonDelayJob<Object> clearJwkJob = new RedissonDelayJob<>();
        clearJwkJob.setJobId(UUID.randomUUID().toString().replace("-", ""));
        clearJwkJob.setData(null);
        clearJwkJob.setDelayTime(7L);
        clearJwkJob.setTimeUnit(TimeUnit.DAYS);
        clearJwkJob.setQueue(RedisConstants.REDISSON_DELAYED_QUEUE_JWK);
        redissonDelayJobQueue.offer(clearJwkJob);
        return new ProcessResult(true, "成功轮换jwk，并开启延时清理旧jwk任务");
    }


}
