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
        this.pathMatcher = new AntPathMatcher();
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

}
