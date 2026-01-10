package com.universe.life.chat.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 配置类
 * 配置聊天消息相关的 Topic 和重试机制
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:43.142.103.248:9092}")
    private String bootstrapServers;

    @Value("${chat.kafka.topic.message:chat-message}")
    private String messageTopic;

    @Value("${chat.kafka.topic.message-partitions:6}")
    private int messagePartitions;

    @Value("${chat.kafka.topic.message-replicas:1}")
    private int messageReplicas;

    @Value("${chat.kafka.topic.broadcast:chat-broadcast}")
    private String broadcastTopic;

    @Value("${chat.kafka.topic.broadcast-partitions:3}")
    private int broadcastPartitions;

    @Value("${chat.kafka.topic.dead-letter:chat-dead-letter}")
    private String deadLetterTopic;

    /**
     * 聊天消息 Topic
     */
    @Bean
    public NewTopic chatMessageTopic() {
        return TopicBuilder.name(messageTopic)
                .partitions(messagePartitions)
                .replicas(messageReplicas)
                .build();
    }

    /**
     * 广播消息 Topic
     */
    @Bean
    public NewTopic chatBroadcastTopic() {
        return TopicBuilder.name(broadcastTopic)
                .partitions(broadcastPartitions)
                .replicas(messageReplicas)
                .build();
    }

    /**
     * 死信队列 Topic
     */
    @Bean
    public NewTopic chatDeadLetterTopic() {
        return TopicBuilder.name(deadLetterTopic)
                .partitions(3)
                .replicas(messageReplicas)
                .build();
    }

    /**
     * Kafka 生产者工厂配置
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "1");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Kafka 模板
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * 重试模板配置
     * 重试策略：3 次，间隔 1s、2s、4s（指数退避）
     */
    @Bean
    public RetryTemplate kafkaRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(4000);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }
}
