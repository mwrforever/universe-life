package com.universe.life.chat.service.impl;

import com.universe.life.chat.service.SensitiveWordFilter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 DFA (Deterministic Finite Automaton) 算法的敏感词过滤器
 * 
 * <p>使用字典树（Trie）实现高效的多模式匹配，时间复杂度 O(n)
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Service
public class DfaSensitiveWordFilter implements SensitiveWordFilter {

    private static final String REDIS_SENSITIVE_WORDS_KEY = "chat:sensitive:words";
    private static final String IS_END_KEY = "isEnd";

    private final StringRedisTemplate redisTemplate;

    @Value("${chat.sensitive-word.file-path:sensitive-words.txt}")
    private String sensitiveWordFilePath;

    @Value("${chat.sensitive-word.use-redis:false}")
    private boolean useRedis;

    /**
     * DFA 状态机根节点
     */
    private volatile Map<String, Object> sensitiveWordMap = new ConcurrentHashMap<>();

    public DfaSensitiveWordFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        reload();
    }

    @Override
    public boolean containsSensitiveWord(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        return !findSensitiveWords(content).isEmpty();
    }

    @Override
    public List<String> findSensitiveWords(String content) {
        if (content == null || content.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> sensitiveWords = new ArrayList<>();
        String lowerContent = content.toLowerCase();
        int length = lowerContent.length();
        for (int i = 0; i < length; i++) {
            int matchLength = checkSensitiveWord(lowerContent, i);
            if (matchLength > 0) {
                String word = content.substring(i, i + matchLength);
                if (!sensitiveWords.contains(word)) {
                    sensitiveWords.add(word);
                }
                i += matchLength - 1;
            }
        }
        return sensitiveWords;
    }

    @Override
    public String replaceSensitiveWords(String content, String replacement) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        StringBuilder result = new StringBuilder(content);
        String lowerContent = content.toLowerCase();
        int length = lowerContent.length();
        int offset = 0;
        for (int i = 0; i < length; i++) {
            int matchLength = checkSensitiveWord(lowerContent, i);
            if (matchLength > 0) {
                String replaceStr = replacement.repeat(matchLength);
                result.replace(i + offset, i + offset + matchLength, replaceStr);
                offset += replaceStr.length() - matchLength;
                i += matchLength - 1;
            }
        }
        return result.toString();
    }

    @Override
    public void addSensitiveWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }
        addWordToMap(word.trim().toLowerCase());
        if (useRedis) {
            redisTemplate.opsForSet().add(REDIS_SENSITIVE_WORDS_KEY, word.trim().toLowerCase());
        }
        log.debug("Added sensitive word: {}", word);
    }

    @Override
    public void addSensitiveWords(Collection<String> words) {
        if (words == null || words.isEmpty()) {
            return;
        }
        words.forEach(this::addSensitiveWord);
    }

    @Override
    public void removeSensitiveWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }
        if (useRedis) {
            redisTemplate.opsForSet().remove(REDIS_SENSITIVE_WORDS_KEY, word.trim().toLowerCase());
        }
        reload();
        log.debug("Removed sensitive word: {}", word);
    }

    @Override
    public void reload() {
        Set<String> words = loadSensitiveWords();
        Map<String, Object> newMap = new ConcurrentHashMap<>();
        for (String word : words) {
            addWordToMap(word, newMap);
        }
        this.sensitiveWordMap = newMap;
        log.info("Sensitive word filter reloaded, total words: {}", words.size());
    }

    private Set<String> loadSensitiveWords() {
        Set<String> words = new HashSet<>();
        if (useRedis) {
            Set<String> redisWords = redisTemplate.opsForSet().members(REDIS_SENSITIVE_WORDS_KEY);
            if (redisWords != null && !redisWords.isEmpty()) {
                words.addAll(redisWords);
                return words;
            }
        }
        words.addAll(loadFromFile());
        if (useRedis && !words.isEmpty()) {
            redisTemplate.opsForSet().add(REDIS_SENSITIVE_WORDS_KEY, words.toArray(new String[0]));
        }
        return words;
    }

    private Set<String> loadFromFile() {
        Set<String> words = new HashSet<>();
        try {
            ClassPathResource resource = new ClassPathResource(sensitiveWordFilePath);
            if (!resource.exists()) {
                log.warn("Sensitive word file not found: {}", sensitiveWordFilePath);
                return words;
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String word = line.trim().toLowerCase();
                    if (!word.isEmpty() && !word.startsWith("#")) {
                        words.add(word);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to load sensitive words from file: {}", sensitiveWordFilePath, e);
        }
        return words;
    }

    private void addWordToMap(String word) {
        addWordToMap(word, this.sensitiveWordMap);
    }

    @SuppressWarnings("unchecked")
    private void addWordToMap(String word, Map<String, Object> rootMap) {
        Map<String, Object> currentMap = rootMap;
        for (int i = 0; i < word.length(); i++) {
            String key = String.valueOf(word.charAt(i));
            Object value = currentMap.get(key);
            if (value == null) {
                Map<String, Object> newMap = new ConcurrentHashMap<>();
                currentMap.put(key, newMap);
                currentMap = newMap;
            } else {
                currentMap = (Map<String, Object>) value;
            }
        }
        currentMap.put(IS_END_KEY, "1");
    }

    @SuppressWarnings("unchecked")
    private int checkSensitiveWord(String content, int startIndex) {
        Map<String, Object> currentMap = sensitiveWordMap;
        int matchLength = 0;
        int lastMatchLength = 0;
        for (int i = startIndex; i < content.length(); i++) {
            String key = String.valueOf(content.charAt(i));
            Object value = currentMap.get(key);
            if (value == null) {
                break;
            }
            currentMap = (Map<String, Object>) value;
            matchLength++;
            if ("1".equals(currentMap.get(IS_END_KEY))) {
                lastMatchLength = matchLength;
            }
        }
        return lastMatchLength;
    }
}
