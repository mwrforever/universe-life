-- ============================================
-- Universe Life 分布式聊天系统 - 消息服务数据库表结构
-- 版本: V1.0.0
-- 服务: universe-life-message
-- ============================================

-- -------------------------------------------
-- 1. 好友关系表 (user_friend)
-- 存储用户之间的好友关系，双向存储
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `user_friend` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `friend_id` BIGINT NOT NULL COMMENT '好友ID',
    `remark` VARCHAR(50) DEFAULT NULL COMMENT '好友备注名',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-正常, 0-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
    KEY `idx_friend_id` (`friend_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';

-- -------------------------------------------
-- 2. 好友申请表 (friend_request)
-- 存储好友申请记录
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `friend_request` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `from_user_id` BIGINT NOT NULL COMMENT '申请人ID',
    `to_user_id` BIGINT NOT NULL COMMENT '目标用户ID',
    `message` VARCHAR(200) DEFAULT NULL COMMENT '申请消息/验证信息',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待处理, 1-已接受, 2-已拒绝, 3-已过期',
    `handled_at` DATETIME DEFAULT NULL COMMENT '处理时间',
    `expired_at` DATETIME DEFAULT NULL COMMENT '过期时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_to_user_status` (`to_user_id`, `status`),
    KEY `idx_from_user` (`from_user_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友申请表';

-- -------------------------------------------
-- 3. 群组表 (chat_group)
-- 存储群组基本信息
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `chat_group` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '群组名称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '群组头像URL',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '群组描述',
    `owner_id` BIGINT NOT NULL COMMENT '群主用户ID',
    `group_code` VARCHAR(32) DEFAULT NULL COMMENT '群聊邀请码（唯一）',
    `is_public` TINYINT NOT NULL DEFAULT 0 COMMENT '是否公开群: 0-否, 1-是（公开群可被搜索）',
    `allow_member_invite` TINYINT NOT NULL DEFAULT 1 COMMENT '允许成员邀请: 0-仅群主可邀请, 1-所有成员可邀请',
    `max_members` INT NOT NULL DEFAULT 500 COMMENT '最大成员数',
    `member_count` INT NOT NULL DEFAULT 1 COMMENT '当前成员数',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-正常, 0-已解散',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_code` (`group_code`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_is_public` (`is_public`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组表';

-- -------------------------------------------
-- 4. 群组成员表 (group_member)
-- 存储群组成员关系
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `group_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `group_id` BIGINT NOT NULL COMMENT '群组ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '群内昵称',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0-普通成员, 1-管理员, 2-群主',
    `muted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否被禁言: 0-否, 1-是',
    `muted_until` DATETIME DEFAULT NULL COMMENT '禁言截止时间（NULL表示永久禁言）',
    `inviter_id` BIGINT DEFAULT NULL COMMENT '邀请人ID',
    `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_user` (`group_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组成员表';

-- -------------------------------------------
-- 5. 公共聊天室表 (public_room)
-- 存储公共聊天室信息
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `public_room` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '聊天室名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '聊天室描述',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '聊天室头像URL',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '聊天室分类',
    `max_online` INT NOT NULL DEFAULT 1000 COMMENT '最大同时在线人数',
    `current_online` INT NOT NULL DEFAULT 0 COMMENT '当前在线人数',
    `total_members` INT NOT NULL DEFAULT 0 COMMENT '累计加入人数',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-正常, 0-已关闭',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公共聊天室表';

-- -------------------------------------------
-- 6. 用户聊天设置表 (user_chat_setting)
-- 存储用户的聊天相关设置
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `user_chat_setting` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `allow_public_message` TINYINT NOT NULL DEFAULT 0 COMMENT '允许公共单人会话: 0-否, 1-是',
    `allow_stranger_message` TINYINT NOT NULL DEFAULT 0 COMMENT '允许陌生人消息: 0-否, 1-是',
    `message_notification` TINYINT NOT NULL DEFAULT 1 COMMENT '消息通知: 0-关闭, 1-开启',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户聊天设置表';

-- -------------------------------------------
-- 7. 会话列表表 (conversation)
-- 存储用户的会话列表（最近联系人）
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `conversation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_id` BIGINT NOT NULL COMMENT '目标ID（用户ID/群组ID/聊天室ID）',
    `conversation_type` TINYINT NOT NULL COMMENT '会话类型: 1-私聊, 2-群聊, 3-公共聊天室, 4-公共单人会话',
    `last_message_id` VARCHAR(32) DEFAULT NULL COMMENT '最后一条消息ID',
    `last_message_content` VARCHAR(100) DEFAULT NULL COMMENT '最后一条消息内容摘要',
    `last_message_time` DATETIME DEFAULT NULL COMMENT '最后一条消息时间',
    `unread_count` INT NOT NULL DEFAULT 0 COMMENT '未读消息数',
    `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶: 0-否, 1-是',
    `is_muted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否免打扰: 0-否, 1-是',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target_type` (`user_id`, `target_id`, `conversation_type`),
    KEY `idx_user_updated` (`user_id`, `updated_at`),
    KEY `idx_last_message_time` (`last_message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话列表表';
