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
CREATE TABLE IF NOT EXISTS `trade_order`
(
    `id`                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id`             BIGINT          NOT NULL COMMENT '需求ID（关联task表）',
    `publisher_id`        BIGINT          NOT NULL COMMENT '发布者ID（冗余存储，避免跨服务查询）',
    `acceptor_id`         BIGINT          NOT NULL COMMENT '接单者ID',
    `reward_amount`       BIGINT          NOT NULL COMMENT '悬赏金额（分，冗余存储）',
    `payable_amount`      BIGINT          NOT NULL DEFAULT 0 COMMENT '应付金额（分）',
    `paid_amount`         BIGINT          NOT NULL DEFAULT 0 COMMENT '已支付金额（分）',
    `status`              TINYINT         NOT NULL DEFAULT 0 COMMENT '状态（并集）：0待审批(PENDING) 1已拒绝(REJECTED) 2进行中(PROGRESS) 3待确认(SUBMIT) 4待收款(PAYMENT) 5争议中(DISPUTE) 6付款中(PAYING) 7已完成(COMPLETED) 8已放弃(ABANDONED) 9已取消(CANCELLED) 10待评价(RATE)',
    `appeal_locked`       TINYINT         NOT NULL DEFAULT 0 COMMENT '申诉锁定：0未锁定 1锁定（申诉处理中禁止修改订单）',
    `pre_appeal_status`   TINYINT                  DEFAULT NULL COMMENT '申诉前订单状态（TradeOrderStatusEnum.code）',
    `submit_content`      TEXT COMMENT '提交内容',
    `submit_images`       VARCHAR(1000) COMMENT '提交图片URL（JSON数组）',
    `reject_reason`       VARCHAR(500) COMMENT '拒绝原因',
    `applied_at`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `approved_at`         DATETIME                 DEFAULT NULL COMMENT '审批时间',
    `submitted_at`        DATETIME                 DEFAULT NULL COMMENT '提交时间',
    `completed_at`        DATETIME                 DEFAULT NULL COMMENT '完成时间',
    `created_at`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`             TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `version`             BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    -- 联合索引：按需求ID查询订单列表（发布者查看接单列表）
    -- 查询场景：SELECT * FROM trade_order WHERE task_id = ? AND deleted = 0 ORDER BY status
    KEY `idx_task_status_created` (`task_id`, `status`, `deleted`, `created_at`, `id`),
    -- 联合索引：按接单者查询订单列表（我的接单列表）
    -- 查询场景：SELECT * FROM trade_order WHERE acceptor_id = ? AND deleted = 0 ORDER BY status
    KEY `idx_acceptor_status_created` (`acceptor_id`, `status`, `deleted`, `created_at`, `id`),
    -- 唯一索引：检查用户是否已对某需求接单（防重复接单）
    -- 业务规则：同一用户对同一需求只能有一条未删除的订单
    UNIQUE KEY `uk_task_acceptor` (`task_id`, `acceptor_id`, `deleted`),
    -- 联合索引：按发布者查询订单（发布者查看所有订单）
    -- 查询场景：SELECT * FROM trade_order WHERE publisher_id = ? AND deleted = 0
    KEY `idx_publisher_status_created` (`publisher_id`, `status`, `deleted`, `created_at`, `id`),
    -- 联合索引：按状态和时间查询（管理后台分页查询）
    -- 查询场景：SELECT * FROM trade_order WHERE status = ? AND deleted = 0 ORDER BY created_at DESC
    KEY `idx_status_created` (`status`, `deleted`, `created_at`, `id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='交易订单表';
