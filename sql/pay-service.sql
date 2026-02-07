CREATE DATABASE IF NOT EXISTS universe_life_pay DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE universe_life_pay;

CREATE TABLE IF NOT EXISTS `pay_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `biz_type` VARCHAR(32) NOT NULL COMMENT '业务类型',
    `biz_id` BIGINT NOT NULL COMMENT '业务ID（如订单ID）',
    `request_no` VARCHAR(64) NOT NULL COMMENT '支付请求号（幂等键）',
    `payer_id` BIGINT NOT NULL COMMENT '付款方用户ID',
    `payee_id` BIGINT DEFAULT NULL COMMENT '收款方用户ID',
    `amount` BIGINT NOT NULL COMMENT '支付金额（分）',
    `channel` TINYINT NOT NULL DEFAULT 0 COMMENT '支付渠道：0微信 1支付宝 2银行卡',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0创建 1支付中 2成功 3失败 4关闭 5退款',
    `third_trade_no` VARCHAR(128) DEFAULT NULL COMMENT '第三方交易号',
    `third_prepay_id` VARCHAR(128) DEFAULT NULL COMMENT '第三方预支付ID（如微信prepay_id）',
    `paid_at` DATETIME DEFAULT NULL COMMENT '支付完成时间',
    `extra` TEXT DEFAULT NULL COMMENT '扩展信息（JSON）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `version` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_request_no` (`request_no`, `deleted`),
    KEY `idx_biz` (`biz_type`, `biz_id`, `deleted`, `id`),
    KEY `idx_payer` (`payer_id`, `deleted`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';
