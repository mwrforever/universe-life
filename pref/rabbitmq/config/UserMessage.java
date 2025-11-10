package com.universe.life.rabbitmq.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 用户消息模型
 * <p>
 * 用于用户相关业务的消息传递，包括：
 * 1. 用户注册通知
 * 2. 用户信息变更
 * 3. 用户状态变更
 * 4. 用户权限变更
 * </p>
 *
 * @author BMad Optimizer
 * @since 2025/11/10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class UserMessage extends RabbitMqBaseMessage {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 用户状态（0:禁用 1:正常 2:锁定）
     */
    private Integer status;

    /**
     * 用户类型（0:普通用户 1:VIP用户 2:企业用户）
     */
    private Integer userType;

    /**
     * 操作类型（CREATE, UPDATE, DELETE, STATUS_CHANGE）
     */
    private String operationType;

    /**
     * 操作时间
     */
    private java.time.LocalDateTime operationTime;

    /**
     * 操作者
     */
    private String operator;

    /**
     * 用户变更前的信息（用于审计）
     */
    private UserSnapshot beforeSnapshot;

    /**
     * 用户变更后的信息（用于审计）
     */
    private UserSnapshot afterSnapshot;

    /**
     * 构造函数
     */
    public UserMessage() {
        super();
        this.setMessageType("USER_MESSAGE");
    }

    /**
     * 构造函数
     *
     * @param userId       用户ID
     * @param operationType 操作类型
     */
    public UserMessage(Long userId, String operationType) {
        this();
        this.userId = userId;
        this.operationType = operationType;
        this.operationTime = java.time.LocalDateTime.now();
    }

    /**
     * 创建用户注册消息
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param email    邮箱
     * @return 用户消息
     */
    public static UserMessage createRegisterMessage(Long userId, String username, String email) {
        return new UserMessage(userId, "CREATE")
                .setUsername(username)
                .setEmail(email)
                .setStatus(1)
                .setUserType(0)
                .addExtension("event", "USER_REGISTER");
    }

    /**
     * 创建用户信息更新消息
     *
     * @param userId 用户ID
     * @return 用户消息
     */
    public static UserMessage createUpdateMessage(Long userId) {
        return new UserMessage(userId, "UPDATE")
                .addExtension("event", "USER_UPDATE");
    }

    /**
     * 创建用户状态变更消息
     *
     * @param userId   用户ID
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @param operator  操作者
     * @return 用户消息
     */
    public static UserMessage createStatusChangeMessage(Long userId, Integer oldStatus, Integer newStatus, String operator) {
        UserSnapshot before = new UserSnapshot();
        before.setStatus(oldStatus);

        UserSnapshot after = new UserSnapshot();
        after.setStatus(newStatus);

        return new UserMessage(userId, "STATUS_CHANGE")
                .setStatus(newStatus)
                .setOperator(operator)
                .setBeforeSnapshot(before)
                .setAfterSnapshot(after)
                .addExtension("event", "USER_STATUS_CHANGE")
                .addExtension("oldStatus", oldStatus);
    }

    /**
     * 用户快照类（记录变更前后的状态）
     */
    @Data
    @Accessors(chain = true)
    public static class UserSnapshot {
        private String username;
        private String email;
        private String phone;
        private Integer status;
        private Integer userType;
        private java.time.LocalDateTime updateTime;
    }
}