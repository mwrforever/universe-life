USE universe_life_trade;

ALTER TABLE `trade_order`
    ADD COLUMN `payable_amount` BIGINT NOT NULL DEFAULT 0 COMMENT '应付金额（分）' AFTER `reward_amount`;

 ALTER TABLE `trade_order`
     ADD COLUMN `appeal_locked` TINYINT NOT NULL DEFAULT 0 COMMENT '申诉锁定：0未锁定 1锁定（申诉处理中禁止修改订单）' AFTER `status`;

UPDATE `trade_order`
SET `payable_amount` = `reward_amount`
WHERE `payable_amount` = 0;

UPDATE `trade_order`
SET `real_payment_amount` = 0
WHERE `real_payment_amount` IS NULL;

ALTER TABLE `trade_order`
    CHANGE COLUMN `real_payment_amount` `paid_amount` BIGINT NOT NULL DEFAULT 0 COMMENT '已支付金额（分）';

CREATE INDEX `idx_task_status_created` ON `trade_order` (`task_id`, `status`, `deleted`, `created_at`, `id`);
CREATE INDEX `idx_acceptor_status_created` ON `trade_order` (`acceptor_id`, `status`, `deleted`, `created_at`, `id`);
CREATE INDEX `idx_publisher_status_created` ON `trade_order` (`publisher_id`, `status`, `deleted`, `created_at`, `id`);
CREATE INDEX `idx_status_created` ON `trade_order` (`status`, `deleted`, `created_at`, `id`);
