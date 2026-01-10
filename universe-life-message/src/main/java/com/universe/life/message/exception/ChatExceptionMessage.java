package com.universe.life.message.exception;

import com.universe.life.auth.common.message.ExceptionMessage;

/**
 * 聊天系统异常消息定义接口
 * 扩展 ExceptionMessage，定义聊天系统专用的错误消息
 *
 * @author Kiro
 * @since 2026/01/09
 */
public interface ChatExceptionMessage extends ExceptionMessage {

    // ==================== 连接相关 ====================
    String CONNECTION_LIMIT_EXCEEDED = "服务器连接数已达上限";
    String SESSION_NOT_FOUND = "会话不存在";
    String USER_NOT_ONLINE = "用户不在线";
    String WEBSOCKET_CONNECTION_FAILED = "WebSocket连接失败";
    String HEARTBEAT_TIMEOUT = "心跳超时，连接已断开";

    // ==================== 消息相关 ====================
    String MESSAGE_RATE_LIMITED = "消息发送过于频繁，请稍后再试";
    String SENSITIVE_CONTENT_DETECTED = "消息包含敏感内容";
    String MESSAGE_TOO_LONG = "消息内容超出长度限制";
    String MESSAGE_NOT_FOUND = "消息不存在";
    String MESSAGE_RECALL_TIMEOUT = "消息已超过撤回时限";
    String MESSAGE_SEND_FAILED = "消息发送失败";
    String OFFLINE_MESSAGE_LIMIT_EXCEEDED = "离线消息数量已达上限";

    // ==================== 权限相关 ====================
    String NOT_FRIEND = "对方不是您的好友";
    String NOT_GROUP_MEMBER = "您不是该群组成员";
    String GROUP_MUTED = "您已被禁言";
    String PUBLIC_CHAT_DISABLED = "对方未开启公共会话";
    String NO_PERMISSION = "您没有权限执行此操作";
    String STRANGER_MESSAGE_DISABLED = "对方不接收陌生人消息";

    // ==================== 好友相关 ====================
    String FRIEND_REQUEST_NOT_FOUND = "好友请求不存在";
    String FRIEND_REQUEST_EXPIRED = "好友请求已过期";
    String FRIEND_REQUEST_ALREADY_HANDLED = "好友请求已处理";
    String ALREADY_FRIEND = "已经是好友关系";
    String CANNOT_ADD_SELF = "不能添加自己为好友";
    String FRIEND_REQUEST_ALREADY_SENT = "好友请求已发送，请勿重复申请";

    // ==================== 群组相关 ====================
    String GROUP_NOT_FOUND = "群组不存在";
    String GROUP_FULL = "群组成员已达上限";
    String INVALID_GROUP_CODE = "群聊码无效或已过期";
    String NOT_GROUP_OWNER = "仅群主可执行此操作";
    String NOT_GROUP_ADMIN = "仅管理员可执行此操作";
    String MEMBER_INVITE_NOT_ALLOWED = "该群组不允许成员邀请";
    String ALREADY_GROUP_MEMBER = "已经是群组成员";
    String GROUP_DISSOLVED = "群组已解散";
    String CANNOT_REMOVE_OWNER = "不能移除群主";
    String CANNOT_MUTE_ADMIN = "不能禁言管理员";

    // ==================== 聊天室相关 ====================
    String ROOM_NOT_FOUND = "聊天室不存在";
    String ROOM_CLOSED = "聊天室已关闭";
    String ROOM_FULL = "聊天室人数已满";

    // ==================== 会话相关 ====================
    String CONVERSATION_NOT_FOUND = "会话不存在";

    /**
     * 聊天系统消息格式化工具类
     */
    class ChatFormatter extends Formatter {

        /**
         * 获取好友不存在错误消息
         *
         * @param friendId 好友ID
         * @return 错误消息
         */
        public static String friendNotFound(Long friendId) {
            return format("好友 {0} 不存在", friendId);
        }

        /**
         * 获取群组不存在错误消息
         *
         * @param groupId 群组ID
         * @return 错误消息
         */
        public static String groupNotFound(Long groupId) {
            return format("群组 {0} 不存在", groupId);
        }

        /**
         * 获取聊天室不存在错误消息
         *
         * @param roomId 聊天室ID
         * @return 错误消息
         */
        public static String roomNotFound(Long roomId) {
            return format("聊天室 {0} 不存在", roomId);
        }

        /**
         * 获取限流等待时间消息
         *
         * @param seconds 等待秒数
         * @return 错误消息
         */
        public static String rateLimitWait(int seconds) {
            return format("消息发送过于频繁，请 {0} 秒后再试", seconds);
        }

        /**
         * 获取禁言剩余时间消息
         *
         * @param minutes 剩余分钟数
         * @return 错误消息
         */
        public static String muteRemaining(int minutes) {
            return format("您已被禁言，剩余 {0} 分钟", minutes);
        }
    }
}
