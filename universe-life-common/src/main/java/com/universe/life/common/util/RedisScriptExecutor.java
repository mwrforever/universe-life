package com.universe.life.common.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.util.List;

/**
 * Redis Lua 脚本执行器（基于 Redisson）
 * <p>
 * 提供统一的 Lua 脚本执行接口，支持通过 SHA 或脚本内容执行
 * 优先使用 SHA 执行以提高性能，SHA 不存在时自动回退到脚本内容
 *
 * @author universe-life
 */
@Slf4j
@AllArgsConstructor
public class RedisScriptExecutor {

    private final RedissonClient redissonClient;
    private final LuaScriptPreloader scriptPreloader;

    /**
     * 执行 Lua 脚本并返回结果
     * <p>
     * 优先使用预加载的 SHA 执行，SHA 不存在时使用脚本内容
     *
     * @param scriptFileName 脚本文件名（如: sendCaptcha.lua）
     * @param keys            Redis 键列表
     * @param values          参数值列表
     * @return 执行结果
     */
    public Object execute(String scriptFileName, List<Object> keys, Object... values) {
        RScript script = redissonClient.getScript(StringCodec.INSTANCE);

        try {
            // 尝试使用 SHA 执行
            String sha = scriptPreloader.getSha(scriptFileName);

            if (log.isDebugEnabled()) {
                log.debug("使用 SHA 执行脚本: {}, SHA: {}", scriptFileName, sha);
            }

            return script.evalSha(
                    RScript.Mode.READ_WRITE,
                    sha,
                    RScript.ReturnType.INTEGER,
                    keys,
                    values
            );

        } catch (Exception e) {
            log.warn("使用 SHA 执行脚本失败: {}, 尝试使用脚本内容执行", scriptFileName, e);

            // SHA 执行失败，使用脚本内容执行
            String scriptContent = scriptPreloader.getScriptContent(scriptFileName);

            if (scriptContent == null) {
                throw new IllegalStateException("脚本加载失败：" + scriptFileName);
            }

            return script.eval(
                    RScript.Mode.READ_WRITE,
                    scriptContent,
                    RScript.ReturnType.INTEGER,
                    keys,
                    values
            );
        }
    }

    /**
     * 执行 Lua 脚本并返回 Long 类型结果
     *
     * @param scriptFileName 脚本文件名
     * @param keys           Redis 键列表
     * @param values         参数值列表
     * @return Long 类型结果
     */
    public Long executeForLong(String scriptFileName, List<Object> keys, Object... values) {
        return (Long) execute(scriptFileName, keys, values);
    }

    /**
     * 执行 Lua 脚本并返回 Boolean 类型结果
     * <p>
     * 将返回值转换为 Boolean：非 0 为 true，0 为 false
     *
     * @param scriptFileName 脚本文件名
     * @param keys           Redis 键列表
     * @param values         参数值列表
     * @return Boolean 类型结果
     */
    public Boolean executeForBoolean(String scriptFileName, List<Object> keys, Object... values) {
        Long result = executeForLong(scriptFileName, keys, values);
        return result != null && result == 1L;
    }


    /**
     * 获取原始 RScript 实例，用于高级用法
     *
     * @return RScript 实例
     */
    public RScript getScript() {
        return redissonClient.getScript(StringCodec.INSTANCE);
    }
}
