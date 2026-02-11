package com.universe.life.chat.property;

import com.universe.life.chat.service.SensitiveWordFilter;
import com.universe.life.chat.service.impl.DfaSensitiveWordFilter;
import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 敏感词过滤器属性测试
 * 使用 jqwik 进行属性测试，验证敏感词过滤的正确性
 *
 * Feature: distributed-chat-system
 * Property 9: 敏感内容过滤
 * 
 * @author Kiro
 * @since 2026/01/09
 */
@SpringBootTest
@ActiveProfiles("test")
public class SensitiveWordFilterPropertyTest {

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    /**
     * Property 9: 敏感内容过滤
     * *For any* content containing a sensitive word, containsSensitiveWord should return true
     * 
     * **Validates: Requirements 5.7, 14.4**
     */
    @Property(tries = 100)
    @Label("Property 9: 敏感内容过滤 - 包含敏感词时返回 true")
    void containsSensitiveWordReturnsTrueForSensitiveContent(
            @ForAll("sensitiveWords") String sensitiveWord,
            @ForAll @StringLength(min = 0, max = 50) String prefix,
            @ForAll @StringLength(min = 0, max = 50) String suffix
    ) {
        // Given: 添加敏感词
        sensitiveWordFilter.addSensitiveWord(sensitiveWord);

        // When: 构造包含敏感词的内容
        String content = prefix + sensitiveWord + suffix;

        // Then: 应该检测到敏感词
        assertTrue(sensitiveWordFilter.containsSensitiveWord(content),
                String.format("应该检测到敏感词 '%s' 在内容 '%s' 中", sensitiveWord, content));
    }

    /**
     * Property: 敏感词替换后不再包含敏感词
     * *For any* content with sensitive words, after replacement, the content should not contain the original sensitive words
     * 
     * **Validates: Requirements 5.7, 14.4**
     */
    @Property(tries = 100)
    @Label("敏感词替换后不再包含原敏感词")
    void replacedContentDoesNotContainSensitiveWord(
            @ForAll("sensitiveWords") String sensitiveWord,
            @ForAll @StringLength(min = 0, max = 20) String prefix,
            @ForAll @StringLength(min = 0, max = 20) String suffix
    ) {
        // Given: 添加敏感词
        sensitiveWordFilter.addSensitiveWord(sensitiveWord);
        String content = prefix + sensitiveWord + suffix;

        // When: 替换敏感词
        String replaced = sensitiveWordFilter.replaceSensitiveWords(content, "*");

        // Then: 替换后不应包含原敏感词（忽略大小写）
        assertFalse(replaced.toLowerCase().contains(sensitiveWord.toLowerCase()),
                String.format("替换后不应包含敏感词 '%s'，实际结果: '%s'", sensitiveWord, replaced));
    }

    /**
     * Property: 空内容不包含敏感词
     * *For any* null or empty content, containsSensitiveWord should return false
     * 
     * **Validates: Requirements 5.7**
     */
    @Property(tries = 50)
    @Label("空内容不包含敏感词")
    void emptyContentDoesNotContainSensitiveWord(
            @ForAll("emptyOrNullContent") String content
    ) {
        // When & Then
        assertFalse(sensitiveWordFilter.containsSensitiveWord(content),
                "空内容不应该包含敏感词");
    }

    /**
     * Property: 敏感词检测大小写不敏感
     * *For any* sensitive word, detection should be case-insensitive
     * 
     * **Validates: Requirements 5.7**
     */
    @Property(tries = 100)
    @Label("敏感词检测大小写不敏感")
    void sensitiveWordDetectionIsCaseInsensitive(
            @ForAll("sensitiveWords") String sensitiveWord
    ) {
        // Given: 添加敏感词（小写）
        sensitiveWordFilter.addSensitiveWord(sensitiveWord.toLowerCase());

        // When & Then: 各种大小写变体都应该被检测到
        assertTrue(sensitiveWordFilter.containsSensitiveWord(sensitiveWord.toLowerCase()),
                "小写应该被检测到");
        assertTrue(sensitiveWordFilter.containsSensitiveWord(sensitiveWord.toUpperCase()),
                "大写应该被检测到");
        
        // 混合大小写
        if (sensitiveWord.length() > 1) {
            String mixed = sensitiveWord.substring(0, 1).toUpperCase() + 
                          sensitiveWord.substring(1).toLowerCase();
            assertTrue(sensitiveWordFilter.containsSensitiveWord(mixed),
                    "混合大小写应该被检测到");
        }
    }

    /**
     * Property: findSensitiveWords 返回所有敏感词
     * *For any* content with multiple sensitive words, findSensitiveWords should return all of them
     * 
     * **Validates: Requirements 5.7**
     */
    @Property(tries = 50)
    @Label("findSensitiveWords 返回所有敏感词")
    void findSensitiveWordsReturnsAllMatches(
            @ForAll("sensitiveWordPairs") List<String> sensitiveWords
    ) {
        Assume.that(sensitiveWords.size() >= 2);

        // Given: 添加多个敏感词
        for (String word : sensitiveWords) {
            sensitiveWordFilter.addSensitiveWord(word);
        }

        // When: 构造包含所有敏感词的内容
        String content = String.join(" ", sensitiveWords);
        List<String> found = sensitiveWordFilter.findSensitiveWords(content);

        // Then: 应该找到所有敏感词
        for (String word : sensitiveWords) {
            boolean containsWord = found.stream()
                    .anyMatch(f -> f.equalsIgnoreCase(word));
            assertTrue(containsWord,
                    String.format("应该找到敏感词 '%s'", word));
        }
    }

    /**
     * Property: 替换长度与原敏感词长度一致
     * *For any* sensitive word, the replacement should have the same length as the original word
     * 
     * **Validates: Requirements 5.7**
     */
    @Property(tries = 100)
    @Label("替换长度与原敏感词长度一致")
    void replacementLengthMatchesSensitiveWordLength(
            @ForAll("sensitiveWords") String sensitiveWord
    ) {
        // Given: 添加敏感词
        sensitiveWordFilter.addSensitiveWord(sensitiveWord);
        String content = "prefix " + sensitiveWord + " suffix";

        // When: 替换敏感词
        String replaced = sensitiveWordFilter.replaceSensitiveWords(content, "*");

        // Then: 替换后的内容长度应该与原内容相同
        assertEquals(content.length(), replaced.length(),
                "替换后内容长度应该与原内容相同");
    }

    // ==================== Providers ====================

    @Provide
    Arbitrary<String> sensitiveWords() {
        return Arbitraries.of(
                "badword",
                "spam",
                "abuse",
                "hate",
                "violence",
                "illegal",
                "scam",
                "fraud"
        );
    }

    @Provide
    Arbitrary<String> emptyOrNullContent() {
        return Arbitraries.of(null, "", "   ");
    }

    @Provide
    Arbitrary<List<String>> sensitiveWordPairs() {
        return Arbitraries.of(
                Arrays.asList("badword", "spam"),
                Arrays.asList("abuse", "hate"),
                Arrays.asList("violence", "illegal"),
                Arrays.asList("scam", "fraud", "spam")
        );
    }
}
