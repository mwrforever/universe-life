-- =============================================
-- 任务服务数据库初始化脚本
-- =============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS universe_life_task DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE universe_life_task;

-- =============================================
-- 1. 任务表 (task)
-- =============================================
CREATE TABLE IF NOT EXISTS `task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `title` VARCHAR(100) NOT NULL COMMENT '任务标题',
    `description` TEXT NOT NULL COMMENT '任务描述',
    `reward_amount` BIGINT NOT NULL COMMENT '悬赏金额（分）',
    `deposit_amount` BIGINT NOT NULL COMMENT '保证金金额（分）',
    `deposit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '保证金状态：0-未支付，1-已支付，2-已退款',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `deadline` DATETIME NOT NULL COMMENT '截止时间',
    `max_acceptors` INT NOT NULL COMMENT '最大接单人数',
    `current_acceptors` INT NOT NULL DEFAULT 0 COMMENT '当前接单人数',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '任务状态：0-待审核，1-审核拒绝，2-招募中，3-待支付，4-支付中，5-支付失败，6-进行中，7-已完成，8-已取消，9-已下架，10-待退款，11-退款中，12-退款失败',
    `review_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核，1-审核通过，2-审核拒绝',
    `publisher_id` BIGINT NOT NULL COMMENT '发布者ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `version` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    INDEX `idx_publisher_status_created` (`publisher_id`, `status`, `deleted`, `created_at`, `id`),
    INDEX `idx_category_status_created` (`category_id`, `status`, `deleted`, `created_at`, `id`),
    INDEX `idx_status_created` (`status`, `deleted`, `created_at`, `id`),
    INDEX `idx_review_status_created` (`review_status`, `deleted`, `created_at`, `id`),
    INDEX `idx_deadline_status` (`deadline`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- =============================================
-- 2. 任务分类表 (task_category)
-- =============================================
CREATE TABLE IF NOT EXISTS `task_category` (
    `category_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `code` VARCHAR(50) NOT NULL COMMENT '分类代码',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`category_id`),
    UNIQUE KEY `uk_name` (`name`),
    UNIQUE KEY `uk_code` (`code`),
    INDEX `idx_status` (`status`),
    INDEX `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务分类表';

-- =============================================
-- 3. 任务审核记录表 (task_review_record)
-- =============================================
CREATE TABLE IF NOT EXISTS `task_review_record` (
    `review_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审核ID',
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `reviewer_id` BIGINT NOT NULL COMMENT '审核人ID',
    `status` TINYINT NOT NULL COMMENT '审核状态：1-审核通过，2-审核拒绝',
    `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
    `reviewed_at` DATETIME NOT NULL COMMENT '审核时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`review_id`),
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_reviewer_id` (`reviewer_id`),
    INDEX `idx_reviewed_at` (`reviewed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务审核记录表';

-- =============================================
-- 初始化任务分类数据
-- =============================================
INSERT INTO `task_category` (`name`, `code`, `sort`, `status`) VALUES
('游戏任务', 'GAME', 1, 1),
('企业任务', 'ENTERPRISE', 2, 1),
('校园任务', 'CAMPUS', 3, 1),
('设计创意', 'DESIGN', 4, 1),
('文案写作', 'WRITING', 5, 1),
('数据标注', 'DATA_ANNOTATION', 6, 1),
('问卷调查', 'SURVEY', 7, 1),
('其他任务', 'OTHER', 99, 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);
