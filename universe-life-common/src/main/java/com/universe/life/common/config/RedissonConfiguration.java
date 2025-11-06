package com.universe.life.common.config;

import com.universe.life.common.properties.RedissonProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.config.SubscriptionMode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson多模式自动配置类
 * 支持多层次配置控制和独立Bean注册
 *
 * @author 毛伟然
 * @since 2025/11/6 12:11
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(RedissonProperties.class)
@RequiredArgsConstructor
public class RedissonConfiguration {

    private final RedissonProperties redissonProperties;

    /**
     * 唯一的RedissonClient Bean
     * 根据enabled配置自动选择模式
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    @ConditionalOnProperty(prefix = "spring.redis.redisson", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RedissonClient redissonClient() {
        log.info("开始初始化RedissonClient...");

        Config config = createConfig();
        RedissonClient client = Redisson.create(config);

        log.info("RedissonClient初始化完成，模式: {}", getActiveMode());
        return client;
    }

    /**
     * 创建Redisson配置（主要Bean）
     */
    private Config createConfig() {
        Config config = new Config();
        // 根据启用的模式进行配置
        if (redissonProperties.getCluster().getEnabled()) {
            setupClusterConfig(config);
        } else if (redissonProperties.getMasterSlave().getEnabled()) {
            setupMasterSlaveConfig(config);
        } else if (redissonProperties.getSentinel().getEnabled()) {
            setupSentinelConfig(config);
        } else {
            // 默认单机模式
            setupSingleConfig(config);
        }

        return config;
    }

    /**
     * 获取当前启用的模式名称
     */
    private String getActiveMode() {
        if (redissonProperties.getCluster().getEnabled()) {
            return "集群模式";
        } else if (redissonProperties.getMasterSlave().getEnabled()) {
            return "主从模式";
        } else if (redissonProperties.getSentinel().getEnabled()) {
            return "哨兵模式";
        } else {
            return "单机模式";
        }
    }

    /**
     * 配置单机模式
     */
    private void setupSingleConfig(Config config) {
        RedissonProperties.SingleServerConfig singleConfig = redissonProperties.getSingle();

        var singleServerConfig = config.useSingleServer()
                .setAddress(singleConfig.getAddress())
                .setDatabase(singleConfig.getDatabase())
                .setConnectionPoolSize(redissonProperties.getConnectionPoolSize())
                .setConnectTimeout(redissonProperties.getConnectTimeout())
                .setTimeout(redissonProperties.getTimeout());

        // 密码配置
        if (redissonProperties.getPassword() != null && !redissonProperties.getPassword().trim().isEmpty()) {
            singleServerConfig.setPassword(redissonProperties.getPassword());
        }

        log.debug("单机Redis配置完成，地址: {}", singleConfig.getAddress());
    }

    /**
     * 配置集群模式
     */
    private void setupClusterConfig(Config config) {
        RedissonProperties.ClusterConfig clusterConfig = redissonProperties.getCluster();

        var clusterServersConfig = config.useClusterServers()
                .addNodeAddress(clusterConfig.getNodes().toArray(new String[0]))
                .setScanInterval(clusterConfig.getScanInterval())
                .setReadMode(getReadMode(redissonProperties.getReadMode()))
                .setSubscriptionMode(getSubscriptionMode(redissonProperties.getSubscriptionMode()));

        // 密码配置
        if (redissonProperties.getPassword() != null && !redissonProperties.getPassword().trim().isEmpty()) {
            clusterServersConfig.setPassword(redissonProperties.getPassword());
        }

        log.debug("集群Redis配置完成，节点: {}", clusterConfig.getNodes());
    }

    /**
     * 配置主从模式
     */
    private void setupMasterSlaveConfig(Config config) {
        RedissonProperties.MasterSlaveConfig masterSlaveConfig = redissonProperties.getMasterSlave();

        var masterSlaveServersConfig = config.useMasterSlaveServers()
                .setMasterAddress(masterSlaveConfig.getMasterAddress())
                .addSlaveAddress(masterSlaveConfig.getSlaveAddresses().toArray(new String[0]))
                .setReadMode(getReadMode(redissonProperties.getReadMode()))
                .setSubscriptionMode(getSubscriptionMode(redissonProperties.getSubscriptionMode()));

        // 密码配置
        if (redissonProperties.getPassword() != null && !redissonProperties.getPassword().trim().isEmpty()) {
            masterSlaveServersConfig.setPassword(redissonProperties.getPassword());
        }

        log.debug("主从Redis配置完成，主节点: {}", masterSlaveConfig.getMasterAddress());
    }

    /**
     * 配置哨兵模式
     */
    private void setupSentinelConfig(Config config) {
        RedissonProperties.SentinelConfig sentinelConfig = redissonProperties.getSentinel();

        var sentinelServersConfig = config.useSentinelServers()
                .addSentinelAddress(sentinelConfig.getSentinelAddresses().toArray(new String[0]))
                .setMasterName(sentinelConfig.getMasterName())
                .setReadMode(getReadMode(redissonProperties.getReadMode()))
                .setSubscriptionMode(getSubscriptionMode(redissonProperties.getSubscriptionMode()));

        // 密码配置
        if (redissonProperties.getPassword() != null && !redissonProperties.getPassword().trim().isEmpty()) {
            sentinelServersConfig.setPassword(redissonProperties.getPassword());
        }

        log.debug("哨兵Redis配置完成，主服务: {}", sentinelConfig.getMasterName());
    }

    /**
     * 获取读取模式
     */
    private ReadMode getReadMode(String readMode) {
        return switch (readMode.toUpperCase()) {
            case "SLAVE" -> ReadMode.SLAVE;
            case "MASTER_SLAVE" -> ReadMode.MASTER_SLAVE;
            default -> ReadMode.MASTER;
        };
    }

    /**
     * 获取订阅模式
     */
    private SubscriptionMode getSubscriptionMode(String subscriptionMode) {
        if (subscriptionMode == null) {
            return SubscriptionMode.MASTER;
        }
        if (subscriptionMode.equalsIgnoreCase("SLAVE")) {
            return SubscriptionMode.SLAVE;
        }
        return SubscriptionMode.MASTER;
    }
}