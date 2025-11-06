package com.universe.life.common.strategy;

/**
 * @author 毛伟然
 * @since 2025/11/6 10:14
 */
public interface RedissonDelayStrategy {

    String queue();

    void execute(String job);

}
