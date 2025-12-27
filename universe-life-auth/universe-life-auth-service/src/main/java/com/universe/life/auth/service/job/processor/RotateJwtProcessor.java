package com.universe.life.auth.service.job.processor;

import com.universe.life.auth.common.constants.RedisConstants;
import com.universe.life.auth.service.domain.po.Oauth2Jwk;
import com.universe.life.auth.service.enums.JwkState;
import com.universe.life.auth.service.manager.JwkManager;
import com.universe.life.auth.service.service.IOauth2JwkService;
import com.universe.life.common.job.delayjob.RedissonDelayJob;
import com.universe.life.common.job.delayjob.RedissonDelayJobQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author 毛伟然
 * @since 2025/11/6 09:17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RotateJwtProcessor implements BasicProcessor {

    private final JwkManager jwkManager;
    private final RedissonDelayJobQueue redissonDelayJobQueue;
    private final IOauth2JwkService oauth2JwkService;

    @Override
    public ProcessResult process(TaskContext taskContext) throws Exception {
        List<String> allPrimaryKids = jwkManager.allPrimaryKids();
        // 执行轮换任务
        jwkManager.rotate();
        // 将数据库中现在生效的jwk设置为只能解析不可签发
        boolean updated = oauth2JwkService.lambdaUpdate()
                .set(Oauth2Jwk::getState, JwkState.RESOLVED)
                .eq(Oauth2Jwk::getKid, allPrimaryKids)
                .update();
        if (!updated) {
            log.error("轮换jwk时，设置旧jwk为RESOLVED状态失败，kid列表：{}", allPrimaryKids);
        }
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
