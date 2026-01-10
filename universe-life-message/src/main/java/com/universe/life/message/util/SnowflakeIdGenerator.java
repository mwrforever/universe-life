package com.universe.life.message.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 雪花算法ID生成器
 * 使用 Hutool 的 Snowflake 实现
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Component
public class SnowflakeIdGenerator {

    private final Snowflake snowflake;

    public SnowflakeIdGenerator(
            @Value("${chat.snowflake.worker-id:1}") long workerId,
            @Value("${chat.snowflake.datacenter-id:1}") long datacenterId) {
        this.snowflake = IdUtil.getSnowflake(workerId, datacenterId);
    }

    /**
     * 生成唯一ID（Long类型）
     */
    public long nextId() {
        return snowflake.nextId();
    }

    /**
     * 生成唯一ID（String类型）
     */
    public String nextIdStr() {
        return snowflake.nextIdStr();
    }
}
