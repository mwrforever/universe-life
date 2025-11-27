package com.universe.life.common.util;

import lombok.Getter;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ant 风格路径匹配工具类
 *
 * <p>基于 Spring 的 AntPathMatcher 实现，支持高级路径模式匹配功能：
 * <ul>
 *   <li>* 匹配任意数量的字符（但不包括路径分隔符）</li>
 *   <li>** 匹配任意数量的字符，包括路径分隔符（多层路径）</li>
 *   <li>? 匹配单个字符</li>
 *   <li>{spring:[a-z]+} 正则表达式匹配</li>
 *   <li>{*variable} 通配符捕获</li>
 * </ul>
 *
 * <p>本类为容器化管理设计，可通过依赖注入使用。
 *
 * @author 毛伟然
 * @since 2025/11/18 14:15
 */
@Getter
public class AntRequestMatchUtil {


    private final AntPathMatcher pathMatcher;

    /**
     * 构造函数 - 使用默认配置
     */
    public AntRequestMatchUtil() {
        this.pathMatcher = new AntPathMatcher(":"); // 使用冒号作为分隔符支持权限标识符
    }

    /**
     * 检查请求路径是否匹配指定的 Ant 模式
     *
     * @param requestPath 请求路径，如 "/api/users/123"
     * @param pattern     Ant 模式，如 "/api/users/**" 或 "/api/users/*"
     * @return 是否匹配
     */
    public boolean match(String requestPath, String pattern) {
        if (requestPath == null || pattern == null) {
            return false;
        }
        return pathMatcher.match(pattern, requestPath);
    }

    /**
     * 检查请求路径是否匹配任意一个指定的 Ant 模式
     *
     * @param requestPath 请求路径
     * @param patterns    Ant 模式集合
     * @return 是否匹配
     */
    public boolean matchAny(String requestPath, Collection<String> patterns) {
        if (requestPath == null || patterns == null || patterns.isEmpty()) {
            return false;
        }
        return patterns.stream()
                .anyMatch(pattern -> match(requestPath, pattern));
    }

    /**
     * 检查请求路径是否匹配所有指定的 Ant 模式
     *
     * @param requestPath 请求路径
     * @param patterns    Ant 模式集合
     * @return 是否匹配所有模式
     */
    public boolean matchAll(String requestPath, Collection<String> patterns) {
        if (requestPath == null || patterns == null || patterns.isEmpty()) {
            return false;
        }
        return patterns.stream()
                .allMatch(pattern -> match(requestPath, pattern));
    }

    /**
     * 提取路径中的变量值
     *
     * @param pattern     包含变量的模式，如 "/api/users/{id}/profile/{type}"
     * @param requestPath 请求路径，如 "/api/users/123/profile/avatar"
     * @return 提取的变量映射，如 {"id": "123", "type": "avatar"}
     */
    public Map<String, String> extractUriTemplateVariables(String pattern, String requestPath) {
        if (pattern == null || requestPath == null) {
            return Map.of();
        }
        return pathMatcher.extractUriTemplateVariables(pattern, requestPath);
    }

    /**
     * 检查模式是否包含变量
     *
     * @param pattern Ant 模式
     * @return 是否包含变量
     */
    public boolean isPattern(String pattern) {
        return pattern != null && pathMatcher.isPattern(pattern);
    }

    /**
     * 从模式中提取变量名列表
     *
     * @param pattern 包含变量的模式
     * @return 变量名列表
     */
    public List<String> extractVariableNames(String pattern) {
        if (pattern == null) {
            return List.of();
        }
        // 使用正则表达式提取变量名，支持 {variable} 和 {variable:regex} 格式
        Pattern variablePattern = Pattern.compile("\\{([^}:]+)(?::[^}]*)?}");
        Matcher matcher = variablePattern.matcher(pattern);

        List<String> variableNames = new ArrayList<>();
        while (matcher.find()) {
            variableNames.add(matcher.group(1));
        }

        return variableNames;
    }

    /**
     * 比较两个模式的优先级（更具体的模式优先级更高）
     *
     * @param pattern1   模式1
     * @param pattern2   模式2
     * @param lookupPath 用于比较的查找路径
     * @return 比较结果
     */
    public int comparePatterns(String pattern1, String pattern2, String lookupPath) {
        return pathMatcher.getPatternComparator(lookupPath).compare(pattern1, pattern2);
    }

    /**
     * 组合路径模式（用于路径前缀匹配）
     *
     * @param pattern1 第一个模式
     * @param pattern2 第二个模式
     * @return 组合后的模式
     */
    public String combine(String pattern1, String pattern2) {
        if (pattern1 == null) {
            return pattern2;
        }
        if (pattern2 == null) {
            return pattern1;
        }
        return pathMatcher.combine(pattern1, pattern2);
    }

    /**
     * 检查模式是否以指定路径开头
     *
     * @param pattern 模式
     * @param path    路径前缀
     * @return 是否以路径开头
     */
    public boolean startsWith(String pattern, String path) {
        return pattern != null && path != null && pattern.startsWith(path);
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
