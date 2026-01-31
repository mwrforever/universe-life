package com.universe.life.user.privacy.constants;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.List;

public interface RedisConstants {

    // ==================== 缓存键前缀 ====================
    
    // 用户相关
    String USER_INFO_KEY = "sys:user:info:";
    String USER_DETAIL_KEY = "sys:user:detail:";
    String USER_AUTH_PHONE_KEY = "sys:user:auth:phone:";
    String USER_AUTH_EMAIL_KEY = "sys:user:auth:email:";

    // 角色相关
    String ROLE_INFO_KEY_PREFIX = "sys:role:";
    String ROLE_INFO_KEY = "sys:role:info:";
    String USER_ROLES_KEY = "sys:user:roles:";
    String ROLE_OPTIONS_KEY = "sys:role:options";
    
    // 资源权限相关
    String RESOURCE_INFO_KEY_PREFIX = "sys:resource:";
    String RESOURCE_INFO_KEY = "sys:resource:info:";
    String ROLE_RESOURCES_KEY = "sys:role:resources:";
    String RESOURCE_TREE_KEY_PREFIX = "sys:resource:tree:";
    
    // 部门相关
    String DEPARTMENT_INFO_KEY_PREFIX = "sys:department:";
    String DEPARTMENT_INFO_KEY = "sys:department:info:";
    String DEPARTMENT_TREE_KEY = "sys:department:tree";
    String DEPARTMENT_OPTIONS_KEY = "sys:department:options";
    
    // 系统用户相关
    String SYS_USER_INFO_KEY = "sys:user:info:";
    
    // 用户部门关联相关
    String USER_DEPARTMENTS_KEY = "sys:user:departments:";
    String DEPARTMENT_USERS_KEY = "sys:department:users:";
    String USER_ROLE_STATUS_KEY = "sys:user:role:status:";

    
    // ==================== 基础过期时间（分钟） ====================
    
    Integer USER_INFO_EXPIRE = 30;          // 用户信息：30-60分钟
    Integer USER_DETAIL_EXPIRE = 35;        // 用户详情：35-65分钟
    Integer USER_AUTH_EXPIRE = 40;          // 用户认证：40-70分钟
    Integer ROLE_INFO_EXPIRE = 45;          // 角色信息：45-75分钟
    Integer USER_ROLES_EXPIRE = 30;         // 用户角色：30-60分钟
    Integer RESOURCE_INFO_EXPIRE = 50;      // 资源权限：50-80分钟
    Integer ROLE_RESOURCES_EXPIRE = 45;     // 角色资源：45-75分钟
    Integer DEPARTMENT_INFO_EXPIRE = 40;    // 部门信息：40-70分钟
    Integer DEPARTMENT_TREE_EXPIRE = 35;    // 部门树：35-65分钟
    Integer SYS_USER_INFO_EXPIRE = 35;      // 系统用户：35-65分钟
    Integer USER_DEPARTMENTS_EXPIRE = 30;   // 用户部门：30-60分钟
    Integer DEPARTMENT_USERS_EXPIRE = 30;   // 部门用户：30-60分钟
    Integer USER_ROLE_STATUS_EXPIRE = 15;   // 用户角色状态：15-45分钟
    Integer RESOURCE_TREE_EXPIRE = 40;      // 资源树：40-70分钟
    Integer ROLE_OPTIONS_EXPIRE = 35;       // 角色选项：35-65分钟
    Integer DEPARTMENT_OPTIONS_EXPIRE = 35; // 部门选项：35-65分钟

    // ==================== 缓存键构建方法 ====================

    /**
     * 构建角色相关缓存键
     * @param roleId 角色ID
     * @param suffix 后缀（如：info, resources）
     * @return 缓存键
     */
    static String buildRoleKey(Long roleId, String suffix) {
        return ROLE_INFO_KEY_PREFIX + roleId + ":" + suffix;
    }
    
    /**
     * 构建资源相关缓存键
     * @param resourceId 资源ID
     * @param suffix 后缀（如：info）
     * @return 缓存键
     */
    static String buildResourceKey(Long resourceId, String suffix) {
        return RESOURCE_INFO_KEY_PREFIX + resourceId + ":" + suffix;
    }
    
    /**
     * 构建部门相关缓存键
     * @param deptId 部门ID
     * @param suffix 后缀（如：info）
     * @return 缓存键
     */
    static String buildDepartmentKey(Long deptId, String suffix) {
        return DEPARTMENT_INFO_KEY_PREFIX + deptId + ":" + suffix;
    }
    
    /**
     * 构建资源树缓存键
     * @param serviceName 服务名称
     * @param resourceType 资源类型
     * @return 缓存键
     */
    static String buildResourceTreeKey(String serviceName, Integer resourceType) {
        String service = StrUtil.isBlank(serviceName) ? "all" : serviceName;
        String type = resourceType == null ? "all" : String.valueOf(resourceType);
        return RESOURCE_TREE_KEY_PREFIX + service + ":" + type;
    }
    
    /**
     * 获取所有可能的资源树缓存键（用于批量删除）
     * @return 所有资源树缓存键列表
     */
    static List<String> getAllResourceTreeKeys() {
        List<String> keys = new ArrayList<>();
        
        // 添加通用的资源树缓存键
        keys.add(buildResourceTreeKey(null, null)); // all:all
        
        // 添加各个微服务的资源树缓存键
        String[] services = {"user-service", "auth-service", "gateway-service", "common-service", 
                            "task-service", "trade-service", "chat-service", "message-service"};
        Integer[] types = {1, 2, 3}; // 1-菜单、2-按钮、3-接口
        
        for (String service : services) {
            keys.add(buildResourceTreeKey(service, null)); // service:all
            for (Integer type : types) {
                keys.add(buildResourceTreeKey(service, type)); // service:type
            }
        }
        
        // 添加各个类型的资源树缓存键
        for (Integer type : types) {
            keys.add(buildResourceTreeKey(null, type)); // all:type
        }
        
        return keys;
    }

    // ==================== 过期时间计算方法 ====================
    
    static int getUserInfoExpire() {
        return USER_INFO_EXPIRE + RandomUtil.randomInt(10, 30);
    }
    
    static int getUserDetailExpire() {
        return USER_DETAIL_EXPIRE + RandomUtil.randomInt(10, 30);
    }
    
    static int getUserAuthExpire() {
        return USER_AUTH_EXPIRE + RandomUtil.randomInt(10, 30);
    }
    
    static int getRoleInfoExpire() {
        return ROLE_INFO_EXPIRE + RandomUtil.randomInt(10, 30);
    }

    static int getUserRolesExpire() {
        return USER_ROLES_EXPIRE + RandomUtil.randomInt(10, 30);
    }
    
    static int getResourceInfoExpire() {
        return RESOURCE_INFO_EXPIRE + RandomUtil.randomInt(10, 30);
    }
    
    static int getRoleResourcesExpire() {
        return ROLE_RESOURCES_EXPIRE + RandomUtil.randomInt(10, 30);
    }
    
    static int getDepartmentInfoExpire() {
        return DEPARTMENT_INFO_EXPIRE + RandomUtil.randomInt(10, 30);
    }
    
    static int getDepartmentTreeExpire() {
        return DEPARTMENT_TREE_EXPIRE + RandomUtil.randomInt(10, 30);
    }
    
    static int getSysUserInfoExpire() {
        return SYS_USER_INFO_EXPIRE + RandomUtil.randomInt(10, 30);
    }
    
    static int getUserDepartmentsExpire() {
        return USER_DEPARTMENTS_EXPIRE + RandomUtil.randomInt(10, 30);
    }
    
    static int getDepartmentUsersExpire() {
        return DEPARTMENT_USERS_EXPIRE + RandomUtil.randomInt(10, 30);
    }
    
    static int getUserRoleStatusExpire() {
        return USER_ROLE_STATUS_EXPIRE + RandomUtil.randomInt(10, 30);
    }
    
    static int getResourceTreeExpire() {
        return RESOURCE_TREE_EXPIRE + RandomUtil.randomInt(10, 30);
    }
    
    static int getRoleOptionsExpire() {
        return ROLE_OPTIONS_EXPIRE + RandomUtil.randomInt(10, 30);
    }
    
    static int getDepartmentOptionsExpire() {
        return DEPARTMENT_OPTIONS_EXPIRE + RandomUtil.randomInt(10, 30);
    }
    
}
