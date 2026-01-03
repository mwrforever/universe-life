package com.universe.life.common.util;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis Lua 脚本预加载器（基于 Redisson）
 * <p>
 * 应用启动时自动加载指定目录下的 Lua 脚本到 Redis，返回 SHA 摘要供后续执行使用
 * 使用 Redisson 的 RScript API，支持脚本缓存和自动 SHA 管理
 * 支持 IDE 本地运行和 JAR 包部署两种环境
 *
 * @author universe-life
 */
@Slf4j
public class LuaScriptPreloader {

    private final RedissonClient redissonClient;

    /**
     * 脚本资源路径，默认为 classpath*:lua/*.lua
     * 使用 classpath*: 可扫描所有依赖包中的脚本
     */
    @Value("${redis.script.location:classpath*:lua/*.lua}")
    private String scriptLocation;

    /**
     * 脚本文件名 -> SHA 摘要映射
     */
    private final Map<String, String> shaMap = new ConcurrentHashMap<>();

    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();


    /**
     * 脚本文件名 -> 脚本内容映射（用于 SHA 不存在时重新加载）
     */
    private final Map<String, String> scriptContentMap = new ConcurrentHashMap<>();

    public LuaScriptPreloader(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 应用启动时预加载所有 Lua 脚本
     */
    @PostConstruct
    public void preload() {
        log.info("开始预加载 Redis Lua 脚本，路径: {}", scriptLocation);

        try {
            Resource[] resources = resolver.getResources(scriptLocation);

            if (resources.length == 0) {
                log.warn("未找到任何 Lua 脚本文件，路径: {}", scriptLocation);
                return;
            }

            for (Resource resource : resources) {
                String fileName = resource.getFilename();

                if (StrUtil.isBlank(fileName)) {
                    log.warn("跳过文件名为空的资源");
                    continue;
                }

                log.debug("正在加载脚本: {}", fileName);
                // 加载脚本
                String scriptContent = readScriptContent(resource);
                loadSingleScript(fileName, scriptContent);
            }

            log.info("Redis Lua 脚本预加载完成，共加载 {} 个脚本", shaMap.size());
            if (log.isDebugEnabled()) {
                shaMap.forEach((fileName, sha) -> log.debug("脚本: {} -> SHA: {}", fileName, sha));
            }

        } catch (IOException e) {
            log.error("预加载 Lua 脚本失败", e);
            throw new IllegalStateException("Redis Lua 脚本预加载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 加载单个 Lua 脚本到 Redis
     *
     * @param fileName      脚本文件名
     * @param scriptContent 脚本资源
     */
    private void loadSingleScript(String fileName, String scriptContent) {
        try {
            // 校验脚本内容
            if (StrUtil.isBlank(scriptContent)) {
                log.warn("跳过空脚本文件: {}", fileName);
                return;
            }
            // 使用 Redisson 加载脚本
            RScript script = redissonClient.getScript(StringCodec.INSTANCE);
            String sha = script.scriptLoad(scriptContent);

            // 保存 SHA 和脚本内容
            shaMap.put(fileName, sha);
            scriptContentMap.put(fileName, scriptContent);

            log.info("✅ 脚本加载成功: {} -> SHA: {}", fileName, sha);

        } catch (Exception e) {
            log.error(" 脚本加载失败: {}", fileName, e);
            throw new IllegalStateException("脚本加载失败: " + fileName, e);
        }
    }

    /**
     * 读取脚本内容，兼容文件系统和 JAR 包环境
     *
     * @param resource 脚本资源
     * @return 脚本内容
     * @throws IOException 读取失败
     */
    private String readScriptContent(Resource resource) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            // 使用缓冲区读取，提高大文件读取性能
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * 根据文件名获取脚本的 SHA 摘要
     * <p>
     * 业务层使用 SHA 执行脚本，避免重复传输脚本内容
     *
     * @param fileName 脚本文件名（如: sendCaptcha.lua）
     * @return SHA 摘要
     * @throws IllegalArgumentException 脚本不存在
     */
    public String getSha(String fileName) {
        return Optional.ofNullable(shaMap.get(fileName))
                .orElseThrow(() -> {
                    log.error("脚本未找到: {}, 已加载脚本: {}", fileName, shaMap.keySet());
                    return new IllegalArgumentException("脚本未预热: " + fileName + "，请检查文件名是否正确");
                });
    }

    /**
     * 获取脚本内容（用于手动执行或调试）
     *
     * @param fileName 脚本文件名
     * @return 脚本内容
     * @throws IllegalArgumentException 脚本不存在
     */
    public String getScriptContent(String fileName) {
        String script = scriptContentMap.get(fileName);
        if (StrUtil.isBlank(script)) {
            // 从文件目录加载
            script = loadScriptFromFile(fileName);
            loadSingleScript(fileName, script);
        }
        return script;
    }

    private String loadScriptFromFile(String fileName) {
        Resource resource = resolver.getResource("classpath*:lua/" + fileName);
        // 获取脚本内容
        try {
            return readScriptContent(resource);
        } catch (IOException e) {
            log.error("读取脚本失败: {}", fileName, e);
            return null;
        }
    }

    /**
     * 获取所有已加载的脚本文件名和 SHA 映射
     *
     * @return 脚本文件名 -> SHA 摘要映射（只读副本）
     */
    public Map<String, String> getAllLoadedScripts() {
        return Map.copyOf(shaMap);
    }

    /**
     * 检查脚本是否已加载
     *
     * @param fileName 脚本文件名
     * @return true-已加载，false-未加载
     */
    public boolean isScriptLoaded(String fileName) {
        return shaMap.containsKey(fileName);
    }

    /**
     * 获取 RScript 实例，用于业务层执行脚本
     *
     * @return RScript 实例
     */
    public RScript getScript() {
        return redissonClient.getScript(StringCodec.INSTANCE);
    }
}