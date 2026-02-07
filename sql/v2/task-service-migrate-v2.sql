USE universe_life_task;

ALTER TABLE `task`
    ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除' AFTER `updated_at`;

ALTER TABLE `task`
    MODIFY COLUMN `version` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号';

CREATE INDEX `idx_publisher_status_created` ON `task` (`publisher_id`, `status`, `deleted`, `created_at`, `task_id`);
CREATE INDEX `idx_category_status_created` ON `task` (`category_id`, `status`, `deleted`, `created_at`, `task_id`);
CREATE INDEX `idx_status_created` ON `task` (`status`, `deleted`, `created_at`, `task_id`);
CREATE INDEX `idx_review_status_created` ON `task` (`review_status`, `deleted`, `created_at`, `task_id`);
CREATE INDEX `idx_deadline_status` ON `task` (`deadline`, `status`, `deleted`);
