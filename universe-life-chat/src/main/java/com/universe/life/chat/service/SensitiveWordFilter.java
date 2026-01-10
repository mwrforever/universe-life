package com.universe.life.chat.service;

/**
 * 敏感词过滤服务接口
 *
 * @author Kiro
 * @since 2026/01/09
 */
public interface SensitiveWordFilter {

    /**
     * 检查内容是否包含敏感词
     *
     * @param content 待检查内容
     * @return 是否包含敏感词
     */
    boolean containsSensitiveWord(String content);

    /**
     * 获取内容中的敏感词
     *
     * @param content 待检查内容
     * @return 敏感词列表，如果没有则返回空列表
     */
    java.util.List<String> findSensitiveWords(String content);

    /**
     * 替换敏感词为指定字符
     *
     * @param content     待处理内容
     * @param replacement 替换字符
     * @return 替换后的内容
     */
    String replaceSensitiveWords(String content, String replacement);

    /**
     * 添加敏感词
     *
     * @param word 敏感词
     */
    void addSensitiveWord(String word);

    /**
     * 批量添加敏感词
     *
     * @param words 敏感词列表
     */
    void addSensitiveWords(java.util.Collection<String> words);

    /**
     * 移除敏感词
     *
     * @param word 敏感词
     */
    void removeSensitiveWord(String word);

    /**
     * 重新加载敏感词库
     */
    void reload();
}
