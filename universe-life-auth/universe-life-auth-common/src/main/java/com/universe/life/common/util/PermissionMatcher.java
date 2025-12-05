package com.universe.life.common.util;

import com.universe.life.common.domain.dto.UserAuthInfo;
import com.universe.life.common.message.ExceptionMessage;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;

import java.util.Collection;

/**
 * @author 毛伟然
 * @since 2025/11/25 14:54
 */
public class PermissionMatcher {

    private final AntPathMatcher pathMatcher;


    public PermissionMatcher() {
        this.pathMatcher = new AntPathMatcher(":");
    }

    public boolean match(String permission) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserAuthInfo userAuthInfo)) {
            throw new ClassCastException(ExceptionMessage.COMMON_ERROR);
        }
        return matchAnyPermission(userAuthInfo.getPrePermissions(), permission);
    }


    /**
     * 权限标识符匹配 - 专门用于 RBAC 权限系统的权限匹配
     *
     * <p>权限标识符格式：Domain:Resource:Action[:Instance]
     * <p>支持的通配符：
     * <ul>
     *   <li>* - 匹配当前层级任意字符串（不能跨越分隔符）</li>
     *   <li>** - 匹配零个或多个层级</li>
     * </ul>
     *
     * @param userPermission   用户持有的权限模式，如 "user:role:*" 或 "sys:**"
     * @param targetPermission 目标权限，如 "user:role:add" 或 "sys:user:add:123"
     * @return 是否匹配
     */
    public boolean matchPermission(String userPermission, String targetPermission) {
        // 空值检查优先于格式验证
        if (userPermission == null || targetPermission == null) {
            return false;
        }

        // 检查空字符串
        if (userPermission.trim().isEmpty() || targetPermission.trim().isEmpty()) {
            return false;
        }

        // 验证权限格式，确保使用冒号分隔符
        validatePermissionFormat(userPermission, "userPermission");
        validatePermissionFormat(targetPermission, "targetPermission");

        return pathMatcher.match(userPermission, targetPermission);
    }

    /**
     * 检查目标权限是否匹配任意一个用户权限模式
     *
     * @param userPermissions  用户持有的权限模式集合
     * @param targetPermission 目标权限
     * @return 是否匹配任意一个权限模式
     */
    public boolean matchAnyPermission(Collection<String> userPermissions, String targetPermission) {
        if (userPermissions == null || userPermissions.isEmpty() || targetPermission == null) {
            return false;
        }
        validatePermissionFormat(targetPermission, "targetPermission");

        return userPermissions.stream()
                .anyMatch(userPermission -> matchPermission(userPermission, targetPermission));
    }

    /**
     * 验证权限标识符格式是否正确
     *
     * @param permission 权限标识符
     * @param fieldName  字段名称（用于错误信息）
     * @throws IllegalArgumentException 如果格式不正确
     */
    private void validatePermissionFormat(String permission, String fieldName) {
        // 检查是否包含连续的冒号（空层级）
        if (permission.contains("::")) {
            throw new IllegalArgumentException(fieldName + " 格式错误：不能包含连续的冒号 '::'，权限: " + permission);
        }

        // 检查是否以冒号开头或结尾
        if (permission.startsWith(":") || permission.endsWith(":")) {
            throw new IllegalArgumentException(fieldName + " 格式错误：不能以冒号开头或结尾，权限: " + permission);
        }
    }

    /**
     * 权限标识符格式验证
     *
     * @param permission 权限标识符
     * @return 是否为有效的权限标识符格式
     */
    public boolean isValidPermissionFormat(String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            return false;
        }

        try {
            validatePermissionFormat(permission, "permission");
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 解析权限标识符的各个部分
     *
     * @param permission 权限标识符，如 "sys:user:add:123"
     * @return 权限组成部分，如 ["sys", "user", "add", "123"]
     */
    public String[] parsePermission(String permission) {
        if (!isValidPermissionFormat(permission)) {
            throw new IllegalArgumentException("无效的权限标识符格式: " + permission);
        }

        return permission.split(":");
    }

    /**
     * 获取权限层级深度
     *
     * @param permission 权限标识符
     * @return 层级深度（至少为2，如 "user:add" 深度为2）
     */
    public int getPermissionDepth(String permission) {
        return parsePermission(permission).length;
    }

    /**
     * 检查权限是否为指定前缀的子权限
     *
     * @param parentPermission 父权限模式，如 "user:role:**"
     * @param childPermission  子权限，如 "user:role:add"
     * @return 是否为子权限
     */
    public boolean isChildPermission(String parentPermission, String childPermission) {
        if (!parentPermission.contains("**")) {
            // 如果不包含 **，则直接进行精确匹配
            return matchPermission(parentPermission, childPermission);
        }

        // 如果包含 **，检查是否为前缀匹配
        String prefix = parentPermission.replace("**", "");
        return childPermission.startsWith(prefix);
    }

}
