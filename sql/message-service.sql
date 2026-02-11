CREATE DATABASE IF NOT EXISTS universe_life_message DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE universe_life_message;

DROP TABLE IF EXISTS `im_message_index`;
DROP TABLE IF EXISTS `im_session_participant`;
DROP TABLE IF EXISTS `im_session_assignment`;
DROP TABLE IF EXISTS `im_session`;

CREATE TABLE `im_session`
(
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `session_no`        VARCHAR(32)  NOT NULL COMMENT '会话编号（业务可见）',
    `context_biz_type`  TINYINT      NULL COMMENT '上下文业务类型：1-任务 2-申诉',
    `context_biz_id`    BIGINT       NULL COMMENT '上下文业务ID：task_id / aftercare_appeal.id',
    `session_type`      TINYINT      NOT NULL DEFAULT 1 COMMENT '会话类型：1-业务双方沟通 2-客服调解',
    `channel`           TINYINT      NOT NULL DEFAULT 1 COMMENT '渠道：1-app 2-web 3-h5 4-小程序',
    `status`            TINYINT      NOT NULL DEFAULT 0 COMMENT '会话状态：0-进行中 1-已结束 2-已冻结',
    `participant_a_id`   BIGINT       NOT NULL COMMENT '参与方A ID（应用层约定：按角色/ID固定排序）',
    `participant_a_role` TINYINT      NOT NULL COMMENT '参与方A 角色：1-发布方 2-接单方 3-平台客服 4-系统',
    `participant_b_id`   BIGINT       NOT NULL COMMENT '参与方B ID（应用层约定：按角色/ID固定排序）',
    `participant_b_role` TINYINT      NOT NULL COMMENT '参与方B 角色：1-发布方 2-接单方 3-平台客服 4-系统',
    `last_msg_seq`      BIGINT       NULL COMMENT '最后一条消息序号',
    `last_msg_id`       VARCHAR(64)  NULL COMMENT '最后一条消息内容ID（MongoDB _id）',
    `last_msg_time`     DATETIME     NULL COMMENT '最后一条消息时间',
    `last_msg_summary`  VARCHAR(200) NULL COMMENT '最后一条消息摘要',
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `version`           BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session_no` (`session_no`),
    UNIQUE KEY `uk_context_type_pair` (`context_biz_type`, `context_biz_id`, `session_type`, `participant_a_role`, `participant_a_id`, `participant_b_role`, `participant_b_id`, `deleted`),
    INDEX `idx_context_status_last` (`context_biz_type`, `context_biz_id`, `status`, `deleted`, `last_msg_time`, `id`),
    INDEX `idx_participant_a_status_last` (`participant_a_role`, `participant_a_id`, `status`, `deleted`, `last_msg_time`, `id`),
    INDEX `idx_participant_b_status_last` (`participant_b_role`, `participant_b_id`, `status`, `deleted`, `last_msg_time`, `id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='两方单聊会话表（支持任务双方沟通与客服调解的多会话）';

CREATE TABLE `im_session_assignment`
(
    `id`           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '分配记录ID',
    `session_id`   BIGINT   NOT NULL COMMENT '会话ID',
    `from_agent_id` BIGINT  NULL COMMENT '原客服ID',
    `to_agent_id`  BIGINT   NULL COMMENT '新客服ID',
    `assign_type`  TINYINT  NOT NULL DEFAULT 1 COMMENT '分配类型：1-首次分配 2-转接 3-释放',
    `reason`       VARCHAR(200) NULL COMMENT '原因',
    `assigned_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    `created_by`   BIGINT   NULL COMMENT '操作者ID（系统/管理员/客服）',
    PRIMARY KEY (`id`),
    INDEX `idx_session_assigned` (`session_id`, `assigned_at`, `id`),
    INDEX `idx_to_agent_assigned` (`to_agent_id`, `assigned_at`, `id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='会话分配历史表';

CREATE TABLE `im_session_participant`
(
    `id`             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id`     BIGINT   NOT NULL COMMENT '会话ID',
    `participant_id` BIGINT   NOT NULL COMMENT '参与方ID（用户/客服）',
    `role`           TINYINT  NOT NULL COMMENT '角色：1-发布方 2-接单方 3-平台客服 4-系统',
    `unread_count`   INT      NOT NULL DEFAULT 0 COMMENT '未读数（快速展示用）',
    `last_read_seq`  BIGINT   NULL COMMENT '最后已读消息序号',
    `is_top`         TINYINT  NOT NULL DEFAULT 0 COMMENT '置顶：0否 1是',
    `is_muted`       TINYINT  NOT NULL DEFAULT 0 COMMENT '免打扰：0否 1是',
    `joined_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `updated_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session_participant_role` (`session_id`, `participant_id`, `role`),
    INDEX `idx_participant_role_updated` (`participant_id`, `role`, `deleted`, `updated_at`, `id`),
    INDEX `idx_session_role` (`session_id`, `role`, `deleted`, `id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='会话参与方表（会话列表/未读）';

CREATE TABLE `im_message_index`
(
    `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id`   BIGINT      NOT NULL COMMENT '会话ID',
    `msg_seq`      BIGINT      NOT NULL COMMENT '会话内自增序号（用于稳定分页）',
    `msg_id`       VARCHAR(64) NOT NULL COMMENT '消息内容ID（MongoDB _id）',
    `client_msg_id` VARCHAR(64) NULL COMMENT '客户端幂等ID',
    `sender_id`    BIGINT      NOT NULL COMMENT '发送者ID',
    `sender_role`  TINYINT     NOT NULL COMMENT '发送者角色：1-发布方 2-接单方 3-平台客服 4-系统',
    `msg_type`     TINYINT     NOT NULL COMMENT '消息类型：1-文本 2-图片 3-文件 4-商品卡片 5-订单卡片 6-系统事件',
    `direction`    TINYINT     NOT NULL COMMENT '方向：1-参与方A->参与方B 2-参与方B->参与方A 3-系统',
    `sent_at`      DATETIME    NOT NULL COMMENT '发送时间',
    `revoked`      TINYINT     NOT NULL DEFAULT 0 COMMENT '是否撤回：0否 1是',
    `created_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session_seq` (`session_id`, `msg_seq`),
    UNIQUE KEY `uk_msg_id` (`msg_id`),
    INDEX `idx_session_sent_seq` (`session_id`, `sent_at`, `msg_seq`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='消息索引表（MySQL索引 + MongoDB内容映射）';
