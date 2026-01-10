-- =====================================================
-- Task Service Database Update Script
-- 任务服务数据库更新脚本（支持Trade服务拆分）
-- =====================================================

USE universe_life_task;

-- =====================================================
-- 更新task表状态枚举说明
-- 原状态：0待审核 1待支付 2进行中 3已完成 4已取消 5审核拒绝 6已下架
-- 新状态：0待审核 1招募中 2待审批 3进行中 4待验收 5待支付 6争议中 7已完成 8已拒绝 9已下架 10待评价
-- =====================================================

-- 为task表添加/优化索引以支持Trade服务的查询
-- 联合索引：按状态和发布者查询（Trade服务查询可接单的需求）
-- 查询场景：SELECT * FROM task WHERE status = 1 AND deleted = 0 (招募中的需求)
-- 注意：如果索引已存在，此语句会报错，可忽略
ALTER TABLE `task` ADD KEY `idx_status_publisher` (`status`, `publisher_id`, `deleted`);

-- 联合索引：按发布者和状态查询（发布者查看自己的需求列表）
-- 查询场景：SELECT * FROM task WHERE publisher_id = ? AND status = ? AND deleted = 0
-- 注意：检查是否已存在类似索引，避免重复
-- ALTER TABLE `task` ADD KEY `idx_publisher_status` (`publisher_id`, `status`, `deleted`);

-- =====================================================
-- 数据迁移说明（可选执行）
-- 将task_acceptance表数据迁移到trade_order表后执行
-- =====================================================

-- 步骤1：确认数据已迁移到trade_order表
-- SELECT COUNT(*) FROM universe_life_trade.trade_order;

-- 步骤2：备份原表（建议在生产环境执行前备份）
-- CREATE TABLE task_acceptance_backup AS SELECT * FROM task_acceptance;
-- CREATE TABLE task_appeal_backup AS SELECT * FROM task_appeal;

-- 步骤3：删除原表（确认数据迁移完成后执行）
-- DROP TABLE IF EXISTS `task_acceptance`;
-- DROP TABLE IF EXISTS `task_appeal`;

-- =====================================================
-- 状态映射参考（Trade Order Status -> Task Status）
-- =====================================================
-- pending (0) -> pending (2)      接单者申请接单
-- progress (1) -> progress (3)    发布者同意接单
-- rejected (6) -> recruiting (1)  发布者拒绝接单
-- submit (2) -> review (4)        接单者提交成果
-- payment (3) -> payment (5)      发布者确认验收
-- dispute (4) -> dispute (6)      发起申诉
-- completed (5) -> completed (7)  支付完成
-- rate (8) -> rate (10)           交易完成待评价
-- abandoned (7) -> recruiting (1) 接单者放弃任务
