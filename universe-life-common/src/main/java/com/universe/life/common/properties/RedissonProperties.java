package com.universe.life.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Redisson基础配置属性
 * 包含所有模式共用的基础配置
 *
 * @author 毛伟然
 * @since 2025/11/6 12:15
 */
@Data
@Component
@ConfigurationProperties(prefix = "universe-life.redisson")
public class RedissonProperties {

    /**
     * Redis连接密码
     */
    private String password = "1010520mao";

    /**
     * 连接池大小
     */
    private Integer connectionPoolSize = 15;

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectTimeout = 10000;

    /**
     * 命令执行超时时间（毫秒）
     */
    private Integer timeout = 3000;

    /**
     * 重试次数
     */
    private Integer retryAttempts = 3;

    /**
     * 重试间隔时间（毫秒）
     */
    private Integer retryInterval = 1500;

    /**
     * 读取模式（集群/主从/哨兵模式使用）
     * SLAVE: 从从节点读取
     * MASTER: 从主节点读取
     * MASTER_SLAVE: 从主从节点读取
     */
    private String readMode = "SLAVE";

    /**
     * 订阅模式（集群/主从/哨兵模式使用）
     * MASTER: 从主节点订阅
     * SLAVE: 从从节点订阅
     */
    private String subscriptionMode = "MASTER";

    /**
     * 单机模式配置
     */
    private SingleServerConfig single = new SingleServerConfig();

    /**
     * 集群模式配置
     */
    private ClusterConfig cluster = new ClusterConfig();

    /**
     * 主从模式配置
     */
    private MasterSlaveConfig masterSlave = new MasterSlaveConfig();

    /**
     * 哨兵模式配置
     */
    private SentinelConfig sentinel = new SentinelConfig();

    /**
     * 单机模式配置
     */
    @Data
    public static class SingleServerConfig {
        /**
         * 是否启用单机模式
         */
        private Boolean enabled = true;  // 默认启用

        /**
         * Redis地址
         */
        private String address = "redis://localhost:16379";

        /**
         * 数据库编号
         */
        private Integer database = 0;
    }

    /**
     * 集群模式配置
     */
    @Data
    public static class ClusterConfig {
        /**
         * 是否启用集群模式
         */
        private Boolean enabled = false;

        /**
         * 节点地址列表
         */
        private java.util.List<String> nodes = new java.util.ArrayList<>();

        /**
         * 集群扫描间隔时间（毫秒）
         */
        private Integer scanInterval = 2000;
    }

    /**
     * 主从模式配置
     */
    @Data
    public static class MasterSlaveConfig {
        /**
         * 是否启用主从模式
         */
        private Boolean enabled = false;

        /**
         * 主节点地址
         */
        private String masterAddress;

        /**
         * 从节点地址列表
         */
        private java.util.List<String> slaveAddresses = new java.util.ArrayList<>();
    }

    /**
     * 哨兵模式配置
     */
    @Data
    public static class SentinelConfig {
        /**
         * 是否启用哨兵模式
         */
        private Boolean enabled = false;

        /**
         * 哨兵地址列表
         */
        private java.util.List<String> sentinelAddresses = new java.util.ArrayList<>();

        /**
         * 哨兵主服务名称
         */
        private String masterName;
    }
}