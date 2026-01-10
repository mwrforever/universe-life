package com.universe.life.chat.service;

/**
 * 消息权限检查器接口
 *
 * @author Kiro
 * @since 2026/01/09
 */
public interface MessagePermissionChecker {

    /**
     * 检查私信消息权限
     * 发送者与接收者必须是好友关系
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     */
    void checkPrivateMessagePermission(Long senderId, Long receiverId);

    /**
     * 检查群聊消息权限
     * 发送者必须是群成员且未被禁言
     *
     * @param senderId 发送者ID
     * @param groupId  群组ID
     */
    void checkGroupMessagePermission(Long senderId, Long groupId);

    /**
     * 检查公共聊天室消息权限
     * 聊天室必须存在且正常
     *
     * @param senderId 发送者ID
     * @param roomId   聊天室ID
     */
    void checkRoomMessagePermission(Long senderId, Long roomId);

    /**
     * 检查公共单人会话消息权限
     * 目标用户必须开启公共会话功能
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     */
    void checkPublicMessagePermission(Long senderId, Long receiverId);
}
