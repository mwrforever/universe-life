package com.universe.life.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis 缓存工具类
 * 提供统一的缓存操作接口，包括读取、写入、删除等功能
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Slf4j
@RequiredArgsConstructor
public class CacheUtil {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 从缓存获取对象
     *
     * @param key   缓存键
     * @param clazz 对象类型
     * @param <T>   泛型类型
     * @return 缓存的对象，如果不存在或异常则返回 null
     */
    public <T> T get(String key, Class<T> clazz) {
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(json)) {
                log.debug("缓存命中: key={}", key);
                return JSONUtil.toBean(json, clazz);
            }
            log.debug("缓存未命中: key={}", key);
            return null;
        } catch (Exception e) {
            log.error("缓存获取失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 从缓存获取列表
     *
     * @param key   缓存键
     * @param clazz 列表元素类型
     * @param <T>   泛型类型
     * @return 缓存的列表，如果不存在或异常则返回 null
     */
    public <T> List<T> getList(String key, Class<T> clazz) {
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(json)) {
                log.debug("列表缓存命中: key={}", key);
                return JSONUtil.toList(json, clazz);
            }
            log.debug("列表缓存未命中: key={}", key);
            return null;
        } catch (Exception e) {
            log.error("缓存获取失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 从缓存获取对象，如果不存在则计算并缓存
     *
     * @param key           缓存键
     * @param clazz         对象类型
     * @param supplier      数据提供者
     * @param expireMinutes 过期时间（分钟）
     * @param <T>           泛型类型
     * @return 缓存的对象或计算后的对象
     */
    public <T> T getOrCompute(String key, Class<T> clazz, Supplier<T> supplier, int expireMinutes) {
        T cached = get(key, clazz);
        if (cached != null) {
            return cached;
        }

        T computed = supplier.get();
        if (computed != null) {
            set(key, computed, expireMinutes);
        }
        return computed;
    }

    /**
     * 从缓存获取列表，如果不存在则计算并缓存
     *
     * @param key           缓存键
     * @param clazz         列表元素类型
     * @param supplier      数据提供者
     * @param expireMinutes 过期时间（分钟）
     * @param <T>           泛型类型
     * @return 缓存的列表或计算后的列表
     */
    public <T> List<T> getListOrCompute(String key, Class<T> clazz, Supplier<List<T>> supplier, int expireMinutes) {
        List<T> cached = getList(key, clazz);
        if (cached != null) {
            return cached;
        }

        List<T> computed = supplier.get();
        if (computed != null) {
            set(key, computed, expireMinutes);
        }
        return computed;
    }

    /**
     * 设置缓存
     *
     * @param key           缓存键
     * @param value         缓存值
     * @param expireMinutes 过期时间（分钟）
     * @param <T>           泛型类型
     */
    public <T> void set(String key, T value, int expireMinutes) {
        try {
            String json = JSONUtil.toJsonStr(value);
            stringRedisTemplate.opsForValue().set(key, json, expireMinutes, TimeUnit.MINUTES);
            log.debug("缓存设置成功: key={}, expire={}分钟", key, expireMinutes);
        } catch (Exception e) {
            log.error("缓存设置失败: key={}", key, e);
        }
    }

    /**
     * 删除缓存
     *
     * @param key 缓存键
     */
    public void delete(String key) {
        try {
            Boolean result = stringRedisTemplate.delete(key);
            if (result) {
                log.debug("缓存删除成功: key={}", key);
            } else {
                log.warn("缓存删除失败或键不存在: key={}", key);
            }
        } catch (Exception e) {
            log.error("缓存删除异常: key={}", key, e);
        }
    }

    /**
     * 批量删除缓存
     *
     * @param keys 缓存键集合
     */
    public void deleteAll(Collection<String> keys) {
        if (CollUtil.isEmpty(keys)) {
            return;
        }
        try {
            Long count = stringRedisTemplate.delete(keys);
            log.debug("批量删除缓存: count={}", count);
        } catch (Exception e) {
            log.error("批量删除缓存异常: keys={}", keys, e);
        }
    }

    /**
     * 检查缓存是否存在
     *
     * @param key 缓存键
     * @return true 如果存在，false 如果不存在或异常
     */
    public boolean exists(String key) {
        try {
            return stringRedisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("检查缓存存在性异常: key={}", key, e);
            return false;
        }
    }

    /**
     * 从缓存获取对象，如果不存在则计算并缓存（带随机过期时间）
     * 过期时间 = baseMinutes + 10-30分钟随机，避免缓存雪崩
     *
     * @param key         缓存键
     * @param clazz       对象类型
     * @param supplier    数据提供者
     * @param baseMinutes 基础过期时间（分钟）
     * @param <T>         泛型类型
     * @return 缓存的对象或计算后的对象
     */
    public <T> T getOrComputeWithRandomExpire(String key, Class<T> clazz, Supplier<T> supplier, int baseMinutes) {
        T cached = get(key, clazz);
        if (cached != null) {
            return cached;
        }

        T computed = supplier.get();
        if (computed != null) {
            // 添加10-30分钟的随机过期时间
            int randomMinutes = 10 + (int) (Math.random() * 21); // 10-30分钟
            int totalMinutes = baseMinutes + randomMinutes;
            set(key, computed, totalMinutes);
            log.debug("缓存设置成功（带随机过期）: key={}, baseMinutes={}, randomMinutes={}, totalMinutes={}",
                    key, baseMinutes, randomMinutes, totalMinutes);
        }
        return computed;
    }

    /**
     * 从缓存获取列表，如果不存在则计算并缓存（带随机过期时间）
     * 过期时间 = baseMinutes + 10-30分钟随机，避免缓存雪崩
     *
     * @param key         缓存键
     * @param clazz       列表元素类型
     * @param supplier    数据提供者
     * @param baseMinutes 基础过期时间（分钟）
     * @param <T>         泛型类型
     * @return 缓存的列表或计算后的列表
     */
    public <T> List<T> getListOrComputeWithRandomExpire(String key, Class<T> clazz, Supplier<List<T>> supplier, int baseMinutes) {
        List<T> cached = getList(key, clazz);
        if (cached != null) {
            return cached;
        }

        List<T> computed = supplier.get();
        if (computed != null) {
            // 添加10-30分钟的随机过期时间
            int randomMinutes = 10 + (int) (Math.random() * 21); // 10-30分钟
            int totalMinutes = baseMinutes + randomMinutes;
            set(key, computed, totalMinutes);
            log.debug("列表缓存设置成功（带随机过期）: key={}, baseMinutes={}, randomMinutes={}, totalMinutes={}",
                    key, baseMinutes, randomMinutes, totalMinutes);
        }
        return computed;
    }
}
