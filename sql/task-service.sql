create database if not exists universe_life_task;

use universe_life_task;

CREATE TABLE `task_category`
(
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`       VARCHAR(50) NOT NULL COMMENT '分类名称',
    `code`       VARCHAR(30) NOT NULL COMMENT '分类编码',
    `sort`       INT         NOT NULL DEFAULT 0 COMMENT '排序',
    `status`     TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='任务分类表';

-- 初始化分类数据
INSERT INTO `task_category` (`name`, `code`, `sort`, `status`)
VALUES ('游戏', 'GAME', 1, 1),
       ('企业', 'ENTERPRISE', 2, 1),
       ('校园', 'CAMPUS', 3, 1),
       ('设计创意', 'DESIGN', 4, 1);


CREATE TABLE `task`
(
    `id`                BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    `publisher_id`      BIGINT          NOT NULL COMMENT '发布者ID',
    `title`             VARCHAR(100)    NOT NULL COMMENT '任务标题',
    `description`       TEXT COMMENT '任务描述',
    `reward_amount`     BIGINT          NOT NULL COMMENT '悬赏金额（分）',
    `reward_type`       TINYINT                  DEFAULT NULL COMMENT '悬赏类型（扩展字段）',
    `deposit_amount`    BIGINT          NOT NULL COMMENT '保证金金额（50%）（分）',
    `deposit_status`    TINYINT         NOT NULL DEFAULT 0 COMMENT '保证金状态：0待支付 1已支付 2已退还 3已结算',
    `category_id`       BIGINT          NOT NULL COMMENT '任务分类ID',
    `deadline`          DATETIME        NOT NULL COMMENT '截止时间',
    `max_acceptors`     INT             NOT NULL DEFAULT 1 COMMENT '最大接受人数',
    `current_acceptors` INT             NOT NULL DEFAULT 0 COMMENT '当前接受人数',
    `status`            TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0待审核 1待支付 2进行中 3已完成 4已取消 5审核拒绝 6已下架',
    `review_status`     TINYINT         NOT NULL DEFAULT 0 COMMENT '审核状态：0待审核 1通过 2拒绝',
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `version`           bigint unsigned NOT NULL DEFAULT 0 COMMENT '版本号',
    PRIMARY KEY (`id`),
    KEY `idx_publisher_id` (`publisher_id`, deleted),
    KEY `idx_category_id` (`category_id`, publisher_id, deleted),
    KEY `idx_status` (`status`, deleted),
    KEY `idx_review_status` (`review_status`, deleted),
    KEY `idx_created_at` (deleted, `created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='任务表';


CREATE TABLE `task_acceptance`
(
    `id`             BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id`        BIGINT          NOT NULL COMMENT '任务ID',
    `acceptor_id`    BIGINT          NOT NULL COMMENT '接受者ID',
    `status`         TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0进行中 1待确认 2已完成 3已放弃 4争议中',
    `submit_content` TEXT COMMENT '提交内容',
    `submit_images`  VARCHAR(1000) COMMENT '提交图片URL（JSON数组）',
    `accepted_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '接受时间',
    `submitted_at`   DATETIME                 DEFAULT NULL COMMENT '提交时间',
    `completed_at`   DATETIME                 DEFAULT NULL COMMENT '完成时间',
    `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`, deleted),
    unique KEY `idx_acceptor_id` (`acceptor_id`, task_id, deleted),
    KEY `idx_status` (`status`, deleted),
    UNIQUE KEY `uk_task_acceptor` (`task_id`, `acceptor_id`, deleted),
    KEY `idx_created_at` (deleted, `created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='任务接受记录表';


CREATE TABLE `task_review`
(
    `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id`       BIGINT   NOT NULL COMMENT '任务ID',
    `reviewer_id`   BIGINT   NOT NULL COMMENT '审核人ID',
    `status`        TINYINT  NOT NULL COMMENT '审核结果：1通过 2拒绝',
    `reject_reason` VARCHAR(500) COMMENT '拒绝原因',
    `reviewed_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_reviewer_id` (`reviewer_id`, task_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='任务审核记录表';


CREATE TABLE `task_appeal`
(
    `id`              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `acceptance_id`   BIGINT   NOT NULL COMMENT '接受记录ID',
    `appellant_id`    BIGINT   NOT NULL COMMENT '申诉人ID',
    `appeal_type`     TINYINT  NOT NULL COMMENT '申诉类型：1接受方申诉 2发布方申诉',
    `reason`          TEXT     NOT NULL COMMENT '申诉原因',
    `evidence_images` VARCHAR(1000) COMMENT '证据图片URL（JSON数组）',
    `status`          TINYINT  NOT NULL DEFAULT 0 COMMENT '状态：0待处理 1已处理',
    `result`          TINYINT           DEFAULT NULL COMMENT '处理结果：1支持申诉方 2驳回',
    `handler_id`      BIGINT            DEFAULT NULL COMMENT '处理人ID',
    `handle_remark`   VARCHAR(500) COMMENT '处理备注',
    `handled_at`      DATETIME          DEFAULT NULL COMMENT '处理时间',
    `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`         TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_acceptance_id` (`acceptance_id`, deleted),
    unique KEY `idx_appellant_id` (`appellant_id`, acceptance_id, deleted),
    KEY `idx_status` (`status`, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='任务申诉表';