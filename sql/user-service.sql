-- ================================
-- Universe Life 用户服务 SQL
-- ================================
-- 创建时间: 2025-11-24
-- 描述: 用户服务相关的数据库表结构，包含用户基本信息和权限管理
-- 版本: v2.0
-- 更新内容: 新增RBAC权限管理功能
CREATE DATABASE IF NOT EXISTS universe_life_user;
USE universe_life_user;
-- ----------------------------
-- 1. 系统用户表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`      VARCHAR(100) NOT NULL COMMENT '用户名',
    `avatar_url`    VARCHAR(500) NULL COMMENT '头像 URL',
    `gender`        TINYINT  DEFAULT 0 COMMENT '性别：0 保密 1 男 2 女',
    `status`        TINYINT  DEFAULT 0 COMMENT '0 正常 1 可接单 2 禁用',
    `last_login_at` DATETIME     NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50)  NULL COMMENT '最后登录IP',
    `created_at`    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       BIT(1)   DEFAULT 0 COMMENT '软删除标记（0 未删除 1 已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`, deleted),
    INDEX `idx_status` (`status`, deleted, created_at),
    INDEX `idx_created_at` (deleted, `created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='系统用户表';

-- ----------------------------
-- 3. 用户详情表
-- ----------------------------
DROP TABLE IF EXISTS `user_detail`;
CREATE TABLE `user_detail`
(
    `id`            BIGINT       NOT NULL COMMENT '用户id',
    `ext`           JSON         NULL COMMENT '扩展字段（JSON）',
    `bio`           TEXT         NULL COMMENT '简介',
    `receive_order` TINYINT  DEFAULT 0 COMMENT '接单数量',
    `birthday`      DATE         NULL COMMENT '生日',
    `province`      VARCHAR(50)  NULL COMMENT '省',
    `city`          VARCHAR(50)  NULL COMMENT '市',
    `country`       VARCHAR(50)  NULL COMMENT '国家',
    `road`          VARCHAR(200) NULL COMMENT '街道地址',
    `address`       VARCHAR(500) NULL COMMENT '详细地址',
    `created_at`    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户详情表';
-- ----------------------------
-- 2. 用户认证表
-- ----------------------------
DROP TABLE IF EXISTS `user_auth`;
CREATE TABLE `user_auth`
(
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`             BIGINT       NOT NULL COMMENT '用户 ID',
    `identification_type` TINYINT      NOT NULL COMMENT '认证类型：0 微信 1 qq 2 支付宝 3 微博 4 用户名 5 手机号 6 邮箱',
    `identification`      VARCHAR(100) NOT NULL COMMENT '认证名',
    `password`            VARCHAR(255) NULL COMMENT 'BCrypt 哈希后的密码',
    `expires_in`          INT          NULL COMMENT '有效期',
    `created_at`          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`             BIT(1)   DEFAULT 0 COMMENT '软删除时间戳（0 表示未删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_identification` (`identification_type`, `identification`, deleted),
    INDEX `idx_user_id` (`user_id`, deleted),
    UNIQUE INDEX `idx_identification` (`identification`, deleted),
    INDEX `idx_created_at` (`created_at`, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户认证表';

-- ================================
-- RBAC 权限管理模块
-- ================================

-- ----------------------------
-- 4. 资源表 (Resource)
-- ----------------------------
-- 描述: 存储系统中所有需要权限控制的资源，包括API接口、页面、按钮等
DROP TABLE IF EXISTS `resource`;
CREATE TABLE `resource`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '资源ID',
    `resource_code` VARCHAR(100) NOT NULL COMMENT '资源编码（唯一标识）',
    `resource_name` VARCHAR(100) NOT NULL COMMENT '资源名称',
    `resource_type` TINYINT      NOT NULL COMMENT '资源类型：0 菜单 1 按钮 2 接口 3 数据',
    `service_name`  VARCHAR(50)  NULL COMMENT '所属微服务名称（如：user-service, chat-service）',
    `url_pattern`   VARCHAR(500) NULL COMMENT 'URL路径模式（如：/api/v1/users/**）',
    `http_method`   VARCHAR(10)  NULL COMMENT 'HTTP方法（GET,POST,PUT,DELETE等，逗号分隔）',
    `parent_id`     BIGINT       NULL COMMENT '父资源ID（用于构建树形结构）',
    `sort_order`    INT      DEFAULT 0 COMMENT '排序序号',
    `status`        TINYINT  DEFAULT 1 COMMENT '状态：0 禁用 1 启用',
    `description`   VARCHAR(500) NULL COMMENT '资源描述',
    `created_at`    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       BIT(1)   DEFAULT 0 COMMENT '软删除标记（0 未删除 1 已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_resource_code` (`resource_code`, deleted),
    INDEX `idx_resource_type` (`resource_type`, deleted, created_at),
    INDEX `idx_service_name` (`service_name`, deleted, created_at),
    INDEX `idx_parent_id` (`parent_id`, deleted),
    INDEX `idx_status` (`status`, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='系统资源表';

-- ----------------------------
-- 5. 角色表 (Role)
-- ----------------------------
-- 描述: 存储系统中所有角色信息
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_code`   VARCHAR(50)  NOT NULL COMMENT '角色编码（唯一标识）',
    `role_name`   VARCHAR(50)  NOT NULL COMMENT '角色名称',
    `role_type`   TINYINT  DEFAULT 0 COMMENT '角色类型：0 系统角色 1 业务角色 2 自定义角色',
    `data_scope`  TINYINT  DEFAULT 0 COMMENT '数据权限范围：0 全部 1 本部门 2 本部门及下级 3 仅自己',
    `sort_order`  INT      DEFAULT 0 COMMENT '排序序号',
    `status`      TINYINT  DEFAULT 1 COMMENT '状态：0 禁用 1 启用',
    `description` VARCHAR(500) NULL COMMENT '角色描述',
    `created_at`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     BIT(1)   DEFAULT 0 COMMENT '软删除标记（0 未删除 1 已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`, deleted),
    INDEX `idx_role_type` (`role_type`, deleted, created_at),
    INDEX `idx_data_scope` (`data_scope`, deleted, created_at),
    INDEX `idx_status` (`status`, deleted, created_at),
    index `idx_created_at` (deleted, `created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='系统角色表';

-- ----------------------------
-- 6. 用户角色关联表 (User Role)
-- ----------------------------
-- 描述: 建立用户与角色的多对多关系
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`
(
    `id`         BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`    BIGINT NOT NULL COMMENT '用户ID',
    `role_id`    BIGINT NOT NULL COMMENT '角色ID',
    `granted_by` BIGINT NULL COMMENT '授权人ID（谁分配的此角色）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_granted_by` (`granted_by`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户角色关联表';

-- ----------------------------
-- 7. 资源角色关联表 (Resource Role)
-- ----------------------------
-- 描述: 建立资源与角色的多对多关系，定义角色可以访问哪些资源
DROP TABLE IF EXISTS `resource_role`;
CREATE TABLE `resource_role`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `resource_id` BIGINT NOT NULL COMMENT '资源ID',
    `role_id`     BIGINT NOT NULL COMMENT '角色ID',
    `granted_by`  BIGINT NULL COMMENT '授权人ID',
    `created_at`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_resource_role_permission` (`resource_id`, `role_id`),
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_granted_by` (`granted_by`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='资源角色关联表';

-- ================================
-- 组织架构管理模块
-- ================================
-- 创建时间: 2025-12-11
-- 描述: 平台员工、部门管理、部门角色关联

-- ----------------------------
-- 8. 部门表 (Department)

-- ----------------------------
-- 描述: 存储组织架构中的部门信息，支持树形结构
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    `parent_id`   BIGINT       DEFAULT 0 COMMENT '父部门ID（0为顶级部门）',
    `dept_code`   VARCHAR(32)  NOT NULL COMMENT '部门编码（唯一标识）',
    `dept_name`   VARCHAR(64)  NOT NULL COMMENT '部门名称',
    `leader_id`   BIGINT       NULL COMMENT '部门负责人ID（关联user.id）',
    `sort_order`  INT          DEFAULT 0 COMMENT '排序序号',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态：0 禁用 1 启用',
    `description` VARCHAR(255) NULL COMMENT '部门描述',
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     BIT(1)       DEFAULT 0 COMMENT '软删除标记（0 未删除 1 已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dept_code` (`dept_code`, `deleted`),
    INDEX `idx_parent_id` (`parent_id`, `deleted`),
    INDEX `idx_leader_id` (`leader_id`, `deleted`),
    INDEX `idx_status` (`status`, `deleted`),
    INDEX `idx_created_at` (`deleted`, `created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='部门表';

-- ----------------------------
-- 9. 用户部门关联表 (User Department)
-- ----------------------------
-- 描述: 建立用户与部门的多对多关系
DROP TABLE IF EXISTS `sys_user_department`;
CREATE TABLE `sys_user_department`
(
    `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       BIGINT   NOT NULL COMMENT '用户ID',
    `department_id` BIGINT   NOT NULL COMMENT '部门ID',
    `is_primary`    TINYINT  DEFAULT 1 COMMENT '是否主部门：0 否 1 是',
    `created_at`    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_department` (`user_id`, `department_id`),
    INDEX `idx_department_id` (`department_id`),
    INDEX `idx_is_primary` (`user_id`, `is_primary`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户部门关联表';

-- ----------------------------
-- 10. 部门角色关联表 (Role Department)
-- ----------------------------
-- 描述: 建立角色与部门的多对多关系，用于定义部门可分配的角色或部门默认角色
DROP TABLE IF EXISTS `sys_role_department`;
CREATE TABLE `sys_role_department`
(
    `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id`       BIGINT   NOT NULL COMMENT '角色ID',
    `department_id` BIGINT   NOT NULL COMMENT '部门ID',
    `created_at`    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_department` (`role_id`, `department_id`),
    INDEX `idx_department_id` (`department_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='部门角色关联表';


-- ----------------------------
-- 11. 平台员工表 (Sys User)
-- ----------------------------
-- 描述: 存储平台内部员工信息，与普通用户表分离
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '员工ID',
    `employee_no`   VARCHAR(32)  NOT NULL COMMENT '工号（唯一标识）',
    `username`      VARCHAR(64)  NOT NULL COMMENT '用户名（登录名）',
    `password`      VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `real_name`     VARCHAR(64)  NULL COMMENT '真实姓名',
    `phone`         VARCHAR(20)  NULL COMMENT '手机号',
    `email`         VARCHAR(128) NULL COMMENT '邮箱',
    `avatar_url`    VARCHAR(255) NULL COMMENT '头像URL',
    `gender`        TINYINT      DEFAULT 0 COMMENT '性别：0 保密 1 男 2 女',
    `status`        TINYINT      DEFAULT 1 COMMENT '状态：0 禁用 1 启用',
    `last_login_at` DATETIME     NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(64)  NULL COMMENT '最后登录IP',
    `created_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       BIT(1)       DEFAULT 0 COMMENT '软删除标记（0 未删除 1 已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_employee_no` (`employee_no`, `deleted`),
    UNIQUE KEY `uk_username` (`username`, `deleted`),
    INDEX `idx_phone` (`phone`, `deleted`),
    INDEX `idx_email` (`email`, `deleted`),
    INDEX `idx_status` (`status`, `deleted`),
    INDEX `idx_created_at` (`deleted`, `created_at`),
    UNIQUE KEY `uk_e_u_p_e` (`employee_no`, `username`, `email`, `phone`, `deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='平台员工表';

