-- =====================================================
-- Aftercare Service Database Script
-- 售后服务数据库脚本（申诉/仲裁）
-- =====================================================

CREATE DATABASE IF NOT EXISTS universe_life_aftercare DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE universe_life_aftercare;

-- =====================================================
-- 申诉表 (aftercare_appeal)
-- 存储接单交易争议申诉记录
-- =====================================================
CREATE TABLE IF NOT EXISTS `aftercare_appeal`
(
    `id`              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    `biz_id`          BIGINT          NOT NULL COMMENT '业务ID',
    `biz_type`        TINYINT         NOT NULL COMMENT '业务类型：1订单 2任务',
    `appellant_id`    BIGINT          NOT NULL COMMENT '申诉人ID',
    `appeal_type`     TINYINT         NOT NULL COMMENT '申诉类型：1成果不符合要求 2发布者恶意拒绝 3其他',
    `reason`          TEXT            NOT NULL COMMENT '申诉原因',
    `evidence_images` VARCHAR(1000)            DEFAULT NULL COMMENT '证据图片URL（JSON数组）',
    `status`          TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0待处理 1已处理',
    `result`          TINYINT                  DEFAULT NULL COMMENT '处理结果：1支持申诉方 2驳回',
    `handler_id`      BIGINT                   DEFAULT NULL COMMENT '处理人ID',
    `handle_remark`   VARCHAR(500)             DEFAULT NULL COMMENT '处理备注',
    `handled_at`      DATETIME                 DEFAULT NULL COMMENT '处理时间',
    `created_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `version`         BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_biz_status` (`biz_type`, `biz_id`, `status`, `deleted`),
    KEY `idx_appellant_status` (`appellant_id`, `status`, `deleted`),
    KEY `idx_status_created` (`status`, `deleted`, `created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='申诉表';
