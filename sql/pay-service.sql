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
    `channel` TINYINT NOT NULL DEFAULT 0 COMMENT '支付渠道：0微信 1支付宝 2银行卡 3余额',
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

CREATE TABLE IF NOT EXISTS `pay_balance_account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `account_type` VARCHAR(16) NOT NULL DEFAULT 'DEFAULT' COMMENT '账户类型（预留）',
    `currency` VARCHAR(8) NOT NULL DEFAULT 'CNY' COMMENT '币种（预留）',
    `available_balance` BIGINT NOT NULL DEFAULT 0 COMMENT '可用余额（分）',
    `frozen_balance` BIGINT NOT NULL DEFAULT 0 COMMENT '冻结余额（分）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `version` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_account` (`user_id`, `account_type`, `currency`, `deleted`),
    KEY `idx_user` (`user_id`, `deleted`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='余额账户表';

CREATE TABLE IF NOT EXISTS `pay_balance_freeze` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `freeze_no` VARCHAR(64) NOT NULL COMMENT '冻结单号',
    `biz_type` VARCHAR(32) NOT NULL COMMENT '业务类型',
    `biz_id` BIGINT NOT NULL COMMENT '业务ID',
    `request_no` VARCHAR(64) NOT NULL COMMENT '请求号（追踪用）',
    `payer_id` BIGINT NOT NULL COMMENT '冻结所属用户ID',
    `amount` BIGINT NOT NULL COMMENT '冻结金额（分）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0冻结中 1已确认扣款 2已解冻 3关闭/失效',
    `expire_at` DATETIME DEFAULT NULL COMMENT '过期时间',
    `extra` TEXT DEFAULT NULL COMMENT '扩展信息（JSON）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `version` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_freeze_no` (`freeze_no`, `deleted`),
    UNIQUE KEY `uk_req_payer` (`request_no`, `payer_id`, `deleted`),
    KEY `idx_request_no` (`request_no`, `deleted`, `id`),
    KEY `idx_biz_payer` (`biz_type`, `biz_id`, `payer_id`, `deleted`, `id`),
    KEY `idx_payer_status` (`payer_id`, `status`, `deleted`, `id`),
    KEY `idx_expire` (`status`, `expire_at`, `deleted`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='余额冻结表';

CREATE TABLE IF NOT EXISTS `pay_balance_flow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `flow_no` VARCHAR(64) NOT NULL COMMENT '流水号',
    `request_no` VARCHAR(64) NOT NULL COMMENT '请求号（幂等键）',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `biz_type` VARCHAR(32) NOT NULL COMMENT '业务类型',
    `biz_id` BIGINT NOT NULL COMMENT '业务ID',
    `action` VARCHAR(32) NOT NULL COMMENT '动作类型',
    `amount` BIGINT NOT NULL COMMENT '金额（分，正数）',
    `available_delta` BIGINT NOT NULL COMMENT '可用余额变化（可正可负）',
    `frozen_delta` BIGINT NOT NULL COMMENT '冻结余额变化（可正可负）',
    `balance_after` BIGINT NOT NULL COMMENT '变更后可用余额（分）',
    `frozen_after` BIGINT NOT NULL COMMENT '变更后冻结余额（分）',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `extra` TEXT DEFAULT NULL COMMENT '扩展信息（JSON）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `version` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_flow_no` (`flow_no`, `deleted`),
    UNIQUE KEY `uk_req_user_action` (`request_no`, `user_id`, `action`, `deleted`),
    KEY `idx_user` (`user_id`, `deleted`, `id`),
    KEY `idx_biz` (`biz_type`, `biz_id`, `deleted`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='余额流水表';
