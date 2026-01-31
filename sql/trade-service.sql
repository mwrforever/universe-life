-- =====================================================
-- Trade Service Database Script
-- 交易服务数据库脚本
-- =====================================================

CREATE DATABASE IF NOT EXISTS universe_life_trade DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE universe_life_trade;

-- =====================================================
-- 交易订单表 (trade_order)
-- 存储接单交易的完整生命周期信息
-- =====================================================
CREATE TABLE `trade_order`
(
    `id`             BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id`        BIGINT          NOT NULL COMMENT '需求ID（关联task表）',
    `publisher_id`   BIGINT          NOT NULL COMMENT '发布者ID（冗余存储，避免跨服务查询）',
    `acceptor_id`    BIGINT          NOT NULL COMMENT '接单者ID',
    `reward_amount`  BIGINT          NOT NULL COMMENT '悬赏金额（分，冗余存储）',
    `status`         TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0待审批 1进行中 2待确认 3待收款 4争议中 5付款中 6已完成 7已拒绝 8已放弃 9待评价',
    `submit_content` TEXT COMMENT '提交内容',
    `submit_images`  VARCHAR(1000) COMMENT '提交图片URL（JSON数组）',
    `reject_reason`  VARCHAR(500) COMMENT '拒绝原因',
    `applied_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `approved_at`    DATETIME                 DEFAULT NULL COMMENT '审批时间',
    `submitted_at`   DATETIME                 DEFAULT NULL COMMENT '提交时间',
    `completed_at`   DATETIME                 DEFAULT NULL COMMENT '完成时间',
    `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `version`        BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    -- 联合索引：按需求ID查询订单列表（发布者查看接单列表）
    -- 查询场景：SELECT * FROM trade_order WHERE task_id = ? AND deleted = 0 ORDER BY status
    KEY `idx_task_status` (`task_id`, `status`, `deleted`),
    -- 联合索引：按接单者查询订单列表（我的接单列表）
    -- 查询场景：SELECT * FROM trade_order WHERE acceptor_id = ? AND deleted = 0 ORDER BY status
    KEY `idx_acceptor_status` (`acceptor_id`, `status`, `deleted`),
    -- 唯一索引：检查用户是否已对某需求接单（防重复接单）
    -- 业务规则：同一用户对同一需求只能有一条未删除的订单
    UNIQUE KEY `uk_task_acceptor` (`task_id`, `acceptor_id`, `deleted`),
    -- 联合索引：按发布者查询订单（发布者查看所有订单）
    -- 查询场景：SELECT * FROM trade_order WHERE publisher_id = ? AND deleted = 0
    KEY `idx_publisher_status` (`publisher_id`, `status`, `deleted`),
    -- 联合索引：按状态和时间查询（管理后台分页查询）
    -- 查询场景：SELECT * FROM trade_order WHERE status = ? AND deleted = 0 ORDER BY created_at DESC
    KEY `idx_status_created` (`status`, `deleted`, `created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='交易订单表';

-- =====================================================
-- 交易申诉表 (trade_appeal)
-- 存储交易争议申诉记录
-- =====================================================
CREATE TABLE `trade_appeal`
(
    `id`              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`        BIGINT   NOT NULL COMMENT '交易订单ID',
    `appellant_id`    BIGINT   NOT NULL COMMENT '申诉人ID',
    `appeal_type`     TINYINT  NOT NULL COMMENT '申诉类型：1接单方申诉 2发布方申诉',
    `reason`          TEXT     NOT NULL COMMENT '申诉原因',
    `evidence_images` VARCHAR(1000) COMMENT '证据图片URL（JSON数组）',
    `status`          TINYINT  NOT NULL DEFAULT 0 COMMENT '状态：0待处理 1已处理',
    `result`          TINYINT           DEFAULT NULL COMMENT '处理结果：1支持申诉方 2驳回',
    `handler_id`      BIGINT            DEFAULT NULL COMMENT '处理人ID',
    `handle_remark`   VARCHAR(500) COMMENT '处理备注',
    `handled_at`      DATETIME          DEFAULT NULL COMMENT '处理时间',
    `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    -- 联合索引：按订单ID查询申诉（检查是否存在待处理申诉）
    -- 查询场景：SELECT COUNT(*) FROM trade_appeal WHERE order_id = ? AND status = 0 AND deleted = 0
    KEY `idx_order_status` (`order_id`, `status`, `deleted`),
    -- 联合索引：按申诉人查询申诉列表
    -- 查询场景：SELECT * FROM trade_appeal WHERE appellant_id = ? AND deleted = 0
    KEY `idx_appellant_status` (`appellant_id`, `status`, `deleted`),
    -- 联合索引：管理后台按状态查询待处理申诉
    -- 查询场景：SELECT * FROM trade_appeal WHERE status = 0 AND deleted = 0 ORDER BY created_at
    KEY `idx_status_created` (`status`, `deleted`, `created_at`),
    -- 唯一索引：同一订单同一申诉人在同一状态下只能有一条记录
    -- 业务规则：防止重复申诉
    UNIQUE KEY `uk_order_appellant_status` (`order_id`, `appellant_id`, `status`, `deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='交易申诉表';
