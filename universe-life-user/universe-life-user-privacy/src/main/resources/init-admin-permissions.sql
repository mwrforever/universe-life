-- =====================================================
-- 管理后台权限初始化SQL脚本
-- 包含：资源权限、角色、管理员用户初始化数据
-- 作者：毛伟然
-- 日期：2025-12-12
-- =====================================================
-- =====================================================
-- 1. 初始化角色数据
-- =====================================================
INSERT INTO `role` (`role_code`, `role_name`, `role_type`, `data_scope`, `sort_order`, `status`, `description`, `created_at`, `updated_at`, `deleted`) VALUES
('super:admin', '超级管理员', 0, 0, 1, 1, '系统超级管理员，拥有所有权限', NOW(), NOW(), 0),
('normal:employee', '普通员工', 0, 3, 2, 1, '普通员工角色，拥有基础操作权限', NOW(), NOW(), 0),
('department', '部门管理员', 0, 2, 3, 1, '部门管理员角色，管理本部门及下级部门', NOW(), NOW(), 0);

-- =====================================================
-- 2. 初始化管理员用户
-- 管理员用户ID: 1999499286626308096
-- 工号格式：UL + 部门编码 + 6位序号
-- 请运行 AdminInitializationTest.generateAdminInsertSQL() 方法生成密码
-- =====================================================
SET @ADMIN_USER_ID = 1999499286626308096;

-- =====================================================
-- 3. 初始化资源权限数据（管理后台接口权限）
-- 资源类型：0 菜单, 1 按钮, 2 接口, 3 数据权限
-- 状态：0 禁用, 1 启用
-- 通配符说明：** 匹配多级，* 只匹配一级
-- =====================================================

-- 3.0 通配符菜单资源（用于权限层级结构）
INSERT INTO `resource` (`resource_code`, `resource_name`, `resource_type`, `service_name`, `url_pattern`, `http_method`, `parent_id`, `sort_order`, `status`, `description`, `created_at`, `updated_at`, `deleted`) VALUES
-- 顶级通配符：匹配用户服务所有资源
('sys:**', '系统所有权限', 0, 'user-service', '/**', '*', NULL, 0, 1, '匹配用户服务下所有资源，**匹配多级', NOW(), NOW(), 0);

-- 获取sys:**的ID作为父级
SET @SYS_ALL_ID = LAST_INSERT_ID();

-- 管理后台通配符
INSERT INTO `resource` (`resource_code`, `resource_name`, `resource_type`, `service_name`, `url_pattern`, `http_method`, `parent_id`, `sort_order`, `status`, `description`, `created_at`, `updated_at`, `deleted`) VALUES
('sys:admin:**', '管理后台所有权限', 0, 'user-service', '/admin/**', '*', @SYS_ALL_ID, 1, 1, '匹配管理后台所有资源', NOW(), NOW(), 0);

SET @ADMIN_ALL_ID = LAST_INSERT_ID();

-- 各模块通配符菜单
INSERT INTO `resource` (`resource_code`, `resource_name`, `resource_type`, `service_name`, `url_pattern`, `http_method`, `parent_id`, `sort_order`, `status`, `description`, `created_at`, `updated_at`, `deleted`) VALUES
('sys:admin:resource:*', '资源管理', 0, 'user-service', '/admin/resource/*', '*', @ADMIN_ALL_ID, 1, 1, '资源管理模块所有操作', NOW(), NOW(), 0),
('sys:admin:resource-role:*', '资源角色关联管理', 0, 'user-service', '/admin/resource-role/*', '*', @ADMIN_ALL_ID, 2, 1, '资源角色关联模块所有操作', NOW(), NOW(), 0),
('sys:admin:role:*', '角色管理', 0, 'user-service', '/admin/role/*', '*', @ADMIN_ALL_ID, 3, 1, '角色管理模块所有操作', NOW(), NOW(), 0),
('sys:admin:role-department:*', '部门角色关联管理', 0, 'user-service', '/admin/role-department/*', '*', @ADMIN_ALL_ID, 4, 1, '部门角色关联模块所有操作', NOW(), NOW(), 0),
('sys:admin:department:*', '部门管理', 0, 'user-service', '/admin/department/*', '*', @ADMIN_ALL_ID, 5, 1, '部门管理模块所有操作', NOW(), NOW(), 0),
('sys:admin:sys-user:*', '平台员工管理', 0, 'user-service', '/admin/sys-user/*', '*', @ADMIN_ALL_ID, 6, 1, '平台员工管理模块所有操作', NOW(), NOW(), 0),
('sys:admin:user:*', '用户管理', 0, 'user-service', '/admin/user/*', '*', @ADMIN_ALL_ID, 7, 1, '用户管理模块所有操作', NOW(), NOW(), 0),
('sys:admin:detail:*', '用户详情管理', 0, 'user-service', '/admin/detail/*', '*', @ADMIN_ALL_ID, 8, 1, '用户详情管理模块所有操作', NOW(), NOW(), 0),
('sys:admin:user-role:*', '用户角色关联管理', 0, 'user-service', '/admin/user-role/*', '*', @ADMIN_ALL_ID, 9, 1, '用户角色关联模块所有操作', NOW(), NOW(), 0);

-- 3.1 资源管理模块 (AdminResourceController)
INSERT INTO `resource` (`resource_code`, `resource_name`, `resource_type`, `service_name`, `url_pattern`, `http_method`, `parent_id`, `sort_order`, `status`, `description`, `created_at`, `updated_at`, `deleted`) VALUES
('sys:admin:resource:add', '创建资源', 2, 'user-service', '/admin/resource', 'POST', NULL, 1, 1, '创建新的系统资源', NOW(), NOW(), 0),
('sys:admin:resource:read', '获取资源详情', 2, 'user-service', '/admin/resource/{id}', 'GET', NULL, 2, 1, '根据ID获取资源详细信息', NOW(), NOW(), 0),
('sys:admin:resource:update', '更新资源', 2, 'user-service', '/admin/resource/{id}', 'PUT', NULL, 3, 1, '更新资源信息', NOW(), NOW(), 0),
('sys:admin:resource:delete', '删除资源', 2, 'user-service', '/admin/resource/{id}', 'DELETE', NULL, 4, 1, '删除资源', NOW(), NOW(), 0),
('sys:admin:resource:list:read', '分页查询资源列表', 2, 'user-service', '/admin/resource/list', 'GET', NULL, 5, 1, '分页查询资源列表', NOW(), NOW(), 0),
('sys:admin:resource:tree:read', '获取资源树', 2, 'user-service', '/admin/resource/tree', 'GET', NULL, 6, 1, '获取资源树形结构', NOW(), NOW(), 0),
('sys:admin:resource:status:update', '修改资源状态', 2, 'user-service', '/admin/resource/{id}/status', 'PATCH', NULL, 7, 1, '修改资源启用/禁用状态', NOW(), NOW(), 0);

-- 3.2 资源角色关联管理模块 (AdminResourceRoleController)
INSERT INTO `resource` (`resource_code`, `resource_name`, `resource_type`, `service_name`, `url_pattern`, `http_method`, `parent_id`, `sort_order`, `status`, `description`, `created_at`, `updated_at`, `deleted`) VALUES
('sys:admin:resource-role:add', '为角色分配资源权限', 2, 'user-service', '/admin/resource-role', 'POST', NULL, 10, 1, '为角色分配资源权限', NOW(), NOW(), 0),
('sys:admin:resource-role:roles:resources:read', '获取角色的资源权限列表', 2, 'user-service', '/admin/resource-role/roles/{roleId}/resources', 'GET', NULL, 11, 1, '获取角色的所有资源权限', NOW(), NOW(), 0),
('sys:admin:resource-role:roles:resources:delete', '移除角色的资源权限', 2, 'user-service', '/admin/resource-role/roles/{roleId}/resources/{resourceId}', 'DELETE', NULL, 12, 1, '移除角色的指定资源权限', NOW(), NOW(), 0),
('sys:admin:resource-role:roles:resources:update', '更新角色的资源权限', 2, 'user-service', '/admin/resource-role/roles/{roleId}/resources', 'PUT', NULL, 13, 1, '更新角色的资源权限（全量替换）', NOW(), NOW(), 0),
('sys:admin:resource-role:resources:roles:read', '获取拥有某资源权限的角色列表', 2, 'user-service', '/admin/resource-role/resources/{resourceId}/roles', 'GET', NULL, 14, 1, '获取拥有指定资源权限的所有角色', NOW(), NOW(), 0),
('sys:admin:resource-role:check:read', '检查角色是否拥有某资源权限', 2, 'user-service', '/admin/resource-role/roles/{roleId}/resources/{resourceId}/check', 'GET', NULL, 15, 1, '检查角色是否拥有指定资源权限', NOW(), NOW(), 0),
('sys:admin:resource-role:roles:resources:tree:read', '获取角色的资源权限树', 2, 'user-service', '/admin/resource-role/roles/{roleId}/resources/tree', 'GET', NULL, 16, 1, '获取角色的资源权限树（带checked状态）', NOW(), NOW(), 0),
('sys:admin:resource-role:batch:add', '批量为角色分配资源权限', 2, 'user-service', '/admin/resource-role/batch', 'POST', NULL, 17, 1, '批量为多个角色分配同一资源权限', NOW(), NOW(), 0),
('sys:admin:resource-role:users:resources:check:read', '检查用户是否拥有某资源权限', 2, 'user-service', '/admin/resource-role/users/{userId}/resources/{resourceCode}/check', 'GET', NULL, 18, 1, '检查用户是否拥有指定资源权限', NOW(), NOW(), 0);

-- 3.3 角色管理模块 (AdminRoleController)
INSERT INTO `resource` (`resource_code`, `resource_name`, `resource_type`, `service_name`, `url_pattern`, `http_method`, `parent_id`, `sort_order`, `status`, `description`, `created_at`, `updated_at`, `deleted`) VALUES
('sys:admin:role:add', '创建角色', 2, 'user-service', '/admin/role', 'POST', NULL, 20, 1, '创建新的角色', NOW(), NOW(), 0),
('sys:admin:role:read', '获取角色详情', 2, 'user-service', '/admin/role/{id}', 'GET', NULL, 21, 1, '根据ID获取角色详细信息', NOW(), NOW(), 0),
('sys:admin:role:update', '更新角色', 2, 'user-service', '/admin/role/{id}', 'PUT', NULL, 22, 1, '更新角色信息', NOW(), NOW(), 0),
('sys:admin:role:delete', '删除角色', 2, 'user-service', '/admin/role/{id}', 'DELETE', NULL, 23, 1, '删除角色', NOW(), NOW(), 0),
('sys:admin:role:list:read', '分页查询角色列表', 2, 'user-service', '/admin/role/list', 'GET', NULL, 24, 1, '分页查询角色列表', NOW(), NOW(), 0),
('sys:admin:role:status:update', '修改角色状态', 2, 'user-service', '/admin/role/{id}/status', 'PATCH', NULL, 25, 1, '修改角色启用/禁用状态', NOW(), NOW(), 0),
('sys:admin:role:resources:read', '获取角色的资源权限', 2, 'user-service', '/admin/role/{id}/resources', 'GET', NULL, 26, 1, '获取角色关联的所有资源权限', NOW(), NOW(), 0),
('sys:admin:role:options:read', '获取角色选项', 2, 'user-service', '/admin/role/options', 'GET', NULL, 27, 1, '获取所有启用的角色选项（用于下拉列表）', NOW(), NOW(), 0);

-- 3.4 部门角色关联管理模块 (AdminRoleDepartmentController)
INSERT INTO `resource` (`resource_code`, `resource_name`, `resource_type`, `service_name`, `url_pattern`, `http_method`, `parent_id`, `sort_order`, `status`, `description`, `created_at`, `updated_at`, `deleted`) VALUES
('sys:admin:role-department:add', '批量分配部门角色', 2, 'user-service', '/admin/role-department', 'POST', NULL, 30, 1, '为角色批量分配部门', NOW(), NOW(), 0),
('sys:admin:role-department:delete', '批量移除部门角色', 2, 'user-service', '/admin/role-department', 'DELETE', NULL, 31, 1, '为角色批量移除部门', NOW(), NOW(), 0),
('sys:admin:role-department:role:read', '获取角色关联的部门', 2, 'user-service', '/admin/role-department/role/{roleId}', 'GET', NULL, 32, 1, '获取角色关联的所有部门', NOW(), NOW(), 0),
('sys:admin:role-department:department:read', '获取部门关联的角色', 2, 'user-service', '/admin/role-department/department/{departmentId}', 'GET', NULL, 33, 1, '获取部门关联的所有角色', NOW(), NOW(), 0);

-- 3.5 部门管理模块 (AdminSysDepartmentController)
INSERT INTO `resource` (`resource_code`, `resource_name`, `resource_type`, `service_name`, `url_pattern`, `http_method`, `parent_id`, `sort_order`, `status`, `description`, `created_at`, `updated_at`, `deleted`) VALUES
('sys:admin:department:add', '创建部门', 2, 'user-service', '/admin/department', 'POST', NULL, 40, 1, '创建新的部门', NOW(), NOW(), 0),
('sys:admin:department:read', '获取部门详情', 2, 'user-service', '/admin/department/{id}', 'GET', NULL, 41, 1, '根据ID获取部门详细信息', NOW(), NOW(), 0),
('sys:admin:department:update', '更新部门', 2, 'user-service', '/admin/department/{id}', 'PUT', NULL, 42, 1, '更新部门信息', NOW(), NOW(), 0),
('sys:admin:department:delete', '删除部门', 2, 'user-service', '/admin/department/{id}', 'DELETE', NULL, 43, 1, '删除部门', NOW(), NOW(), 0),
('sys:admin:department:list:read', '分页查询部门列表', 2, 'user-service', '/admin/department/list', 'GET', NULL, 44, 1, '分页查询部门列表', NOW(), NOW(), 0),
('sys:admin:department:status:update', '修改部门状态', 2, 'user-service', '/admin/department/{id}/status', 'PATCH', NULL, 45, 1, '修改部门启用/禁用状态', NOW(), NOW(), 0),
('sys:admin:department:tree:read', '获取部门树形结构', 2, 'user-service', '/admin/department/tree', 'GET', NULL, 46, 1, '获取所有启用部门的树形结构', NOW(), NOW(), 0),
('sys:admin:department:options:read', '获取部门选项', 2, 'user-service', '/admin/department/options', 'GET', NULL, 47, 1, '获取所有启用的部门选项（用于下拉列表）', NOW(), NOW(), 0);

-- 3.6 平台员工管理模块 (AdminSysUserController)
INSERT INTO `resource` (`resource_code`, `resource_name`, `resource_type`, `service_name`, `url_pattern`, `http_method`, `parent_id`, `sort_order`, `status`, `description`, `created_at`, `updated_at`, `deleted`) VALUES
('sys:admin:sys-user:add', '创建员工', 2, 'user-service', '/admin/sys-user', 'POST', NULL, 50, 1, '创建新的平台员工', NOW(), NOW(), 0),
('sys:admin:sys-user:read', '获取员工详情', 2, 'user-service', '/admin/sys-user/{id}', 'GET', NULL, 51, 1, '根据ID获取员工详细信息', NOW(), NOW(), 0),
('sys:admin:sys-user:update', '更新员工', 2, 'user-service', '/admin/sys-user/{id}', 'PUT', NULL, 52, 1, '更新员工信息', NOW(), NOW(), 0),
('sys:admin:sys-user:delete', '删除员工', 2, 'user-service', '/admin/sys-user/{id}', 'DELETE', NULL, 53, 1, '删除员工', NOW(), NOW(), 0),
('sys:admin:sys-user:list:read', '分页查询员工列表', 2, 'user-service', '/admin/sys-user/list', 'GET', NULL, 54, 1, '分页查询员工列表', NOW(), NOW(), 0),
('sys:admin:sys-user:status:update', '修改员工状态', 2, 'user-service', '/admin/sys-user/{id}/status', 'PATCH', NULL, 55, 1, '修改员工启用/禁用状态', NOW(), NOW(), 0),
('sys:admin:sys-user:password:update', '重置员工密码', 2, 'user-service', '/admin/sys-user/{id}/password', 'PUT', NULL, 56, 1, '重置员工密码', NOW(), NOW(), 0),
('sys:admin:sys-user:options:read', '获取员工选项', 2, 'user-service', '/admin/sys-user/options', 'GET', NULL, 57, 1, '获取所有启用的员工选项（用于下拉列表）', NOW(), NOW(), 0);

-- 3.7 用户管理模块 (AdminUserController)
INSERT INTO `resource` (`resource_code`, `resource_name`, `resource_type`, `service_name`, `url_pattern`, `http_method`, `parent_id`, `sort_order`, `status`, `description`, `created_at`, `updated_at`, `deleted`) VALUES
('sys:admin:user:read', '根据ID查询用户', 2, 'user-service', '/admin/{id}', 'GET', NULL, 60, 1, '管理员根据用户ID获取用户详细信息', NOW(), NOW(), 0),
('sys:admin:user:add', '创建用户', 2, 'user-service', '/admin', 'POST', NULL, 61, 1, '管理员创建新用户', NOW(), NOW(), 0),
('sys:admin:user:list:read', '分页查询用户列表', 2, 'user-service', '/admin/list', 'GET', NULL, 62, 1, '管理员根据条件分页查询用户列表', NOW(), NOW(), 0),
('sys:admin:user:update', '更新用户信息', 2, 'user-service', '/admin/{id}', 'PUT', NULL, 63, 1, '管理员更新用户基本信息和角色信息', NOW(), NOW(), 0),
('sys:admin:user:delete', '删除用户', 2, 'user-service', '/admin/{id}', 'POST', NULL, 64, 1, '管理员软删除指定用户', NOW(), NOW(), 0),
('sys:admin:user:status:read', '获取用户状态', 2, 'user-service', '/admin/status', 'GET', NULL, 65, 1, '管理员根据用户名获取用户状态', NOW(), NOW(), 0),
('sys:admin:user:resetPassword:update', '重置用户密码', 2, 'user-service', '/admin/resetPassword/{id}', 'PUT', NULL, 66, 1, '管理员重置用户密码', NOW(), NOW(), 0),
('sys:admin:user:status:update', '更新用户状态', 2, 'user-service', '/admin/status', 'PUT', NULL, 67, 1, '管理员更新用户状态', NOW(), NOW(), 0),
('sys:admin:user:batch:delete', '批量删除用户', 2, 'user-service', '/admin/batch/delete', 'POST', NULL, 68, 1, '管理员批量软删除用户', NOW(), NOW(), 0),
('sys:admin:user:batch:status:update', '批量更新用户状态', 2, 'user-service', '/admin/batch/status', 'PUT', NULL, 69, 1, '管理员批量更新用户状态', NOW(), NOW(), 0);

-- 3.8 用户详情管理模块 (AdminUserDetailController)
INSERT INTO `resource` (`resource_code`, `resource_name`, `resource_type`, `service_name`, `url_pattern`, `http_method`, `parent_id`, `sort_order`, `status`, `description`, `created_at`, `updated_at`, `deleted`) VALUES
('sys:admin:detail:read', '管理后台获取用户详情', 2, 'user-service', '/admin/detail/{id}', 'GET', NULL, 70, 1, '管理员获取指定用户的详细信息', NOW(), NOW(), 0),
('sys:admin:detail:update', '更新用户详情', 2, 'user-service', '/admin/detail/{id}', 'PUT', NULL, 71, 1, '管理员更新用户详细信息', NOW(), NOW(), 0);

-- 3.9 用户角色关联管理模块 (AdminUserRoleController)
INSERT INTO `resource` (`resource_code`, `resource_name`, `resource_type`, `service_name`, `url_pattern`, `http_method`, `parent_id`, `sort_order`, `status`, `description`, `created_at`, `updated_at`, `deleted`) VALUES
('sys:admin:user-role:add', '为用户分配角色', 2, 'user-service', '/admin/user-role', 'POST', NULL, 80, 1, '为用户分配角色（会替换原有角色）', NOW(), NOW(), 0),
('sys:admin:user-role:users:roles:read', '获取用户的角色列表', 2, 'user-service', '/admin/user-role/users/{userId}/roles', 'GET', NULL, 81, 1, '获取用户的所有角色', NOW(), NOW(), 0),
('sys:admin:user-role:users:roles:delete', '移除用户的角色', 2, 'user-service', '/admin/user-role/users/{userId}/roles/{roleId}', 'DELETE', NULL, 82, 1, '移除用户的指定角色', NOW(), NOW(), 0),
('sys:admin:user-role:users:roles:update', '更新用户的角色', 2, 'user-service', '/admin/user-role/users/{userId}/roles', 'PUT', NULL, 83, 1, '更新用户的角色（全量替换）', NOW(), NOW(), 0),
('sys:admin:user-role:batch:add', '批量为用户分配角色', 2, 'user-service', '/admin/user-role/batch', 'POST', NULL, 84, 1, '批量为多个用户分配同一角色', NOW(), NOW(), 0),
('sys:admin:user-role:check:read', '检查用户是否拥有某角色', 2, 'user-service', '/admin/user-role/users/{userId}/roles/{roleId}/check', 'GET', NULL, 85, 1, '检查用户是否拥有指定角色', NOW(), NOW(), 0),
('sys:admin:user-role:users:permissions:read', '获取用户的所有权限', 2, 'user-service', '/admin/user-role/users/{userId}/permissions', 'GET', NULL, 86, 1, '获取用户的所有角色和资源权限', NOW(), NOW(), 0);

-- =====================================================
-- 4. 为超级管理员角色分配所有资源权限
-- =====================================================

-- 获取超级管理员角色ID并为其分配所有sys:开头的资源
INSERT INTO `resource_role` (`resource_id`, `role_id`, `granted_by`, `created_at`)
SELECT r.id, (SELECT id FROM `role` WHERE role_code = 'super:admin'), @ADMIN_USER_ID, NOW()
FROM `resource` r
WHERE r.resource_code LIKE 'sys:%' AND r.deleted = 0;


-- =====================================================
-- 5. 为管理员用户分配超级管理员角色
-- 管理员用户ID: 1999499286626308096
-- =====================================================
INSERT INTO `sys_user_role` (`sys_user_id`, `role_id`, `granted_by`, `created_at`)
SELECT @ADMIN_USER_ID, r.id, @ADMIN_USER_ID, NOW()
FROM `role` r
WHERE r.role_code = 'super:admin';

-- =====================================================
-- 执行步骤说明
-- =====================================================
-- 1. 确保admin用户已创建（ID: 1999499286626308096）
-- 2. 执行本脚本初始化角色、资源权限和关联关系
-- 3. 执行前请备份数据库
--
-- 通配符说明:
-- sys:**        - 匹配用户服务下所有资源（多级）
-- sys:admin:**  - 匹配管理后台所有资源（多级）
-- sys:admin:resource:* - 匹配资源管理模块所有操作（单级）
--
-- 测试方法位置:
-- universe-life-user-privacy/src/test/java/com/universe/life/user/privacy/AdminInitializationTest.java
-- =====================================================

