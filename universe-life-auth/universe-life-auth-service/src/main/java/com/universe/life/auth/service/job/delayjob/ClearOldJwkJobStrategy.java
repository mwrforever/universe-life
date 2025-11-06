package com.universe.life.auth.service.job.delayjob;

import com.universe.life.auth.service.manager.JwkManager;
import com.universe.life.common.enums.RedissonDelayQueue;
import com.universe.life.common.strategy.RedissonDelayStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author 毛伟然
 * @since 2025/11/6 10:20
 */
@Component
@RequiredArgsConstructor
public class ClearOldJwkJobStrategy implements RedissonDelayStrategy {

    private final JwkManager jwkManager;

    @Override
    public String queue() {
        return RedissonDelayQueue.CLEAR_OLD_JWK_QUEUE.getValue();
    }

    @Override
    public void execute(String job) {
        // 定义任务执行逻辑
        jwkManager.removeOldKey();
    }


}
