package com.universe.life.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Redis 缓存工具类
 * 提供统一的缓存操作接口，包括读取、写入、删除等功能
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
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
                return JSONUtil.toBean(json, clazz);
            }
            return null;
        } catch (Exception e) {
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
                return JSONUtil.toList(json, clazz);
            }
            return null;
        } catch (Exception e) {
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
        } catch (Exception e) {
            // 静默处理异常
        }
    }

    /**
     * 删除缓存
     *
     * @param key 缓存键
     */
    public void delete(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            // 静默处理异常
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
            stringRedisTemplate.delete(keys);
        } catch (Exception e) {
            // 静默处理异常
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
            return false;
        }
    }

    public boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        try {
            Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
            return Boolean.TRUE.equals(success);
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Hash结构操作 ====================

    /**
     * 从Hash缓存获取对象
     *
     * @param key   Hash键
     * @param field 字段名
     * @param clazz 对象类型
     * @param <T>   泛型类型
     * @return 缓存的对象，如果不存在或异常则返回 null
     */
    public <T> T hGet(String key, String field, Class<T> clazz) {
        try {
            Object value = stringRedisTemplate.opsForHash().get(key, field);
            if (value != null) {
                String json = value.toString();
                if (StrUtil.isNotBlank(json)) {
                    return JSONUtil.toBean(json, clazz);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Hash缓存获取列表
     *
     * @param key   Hash键
     * @param field 字段名
     * @param clazz 列表元素类型
     * @param <T>   泛型类型
     * @return 缓存的列表，如果不存在或异常则返回 null
     */
    public <T> List<T> hGetList(String key, String field, Class<T> clazz) {
        try {
            Object value = stringRedisTemplate.opsForHash().get(key, field);
            if (value != null) {
                String json = value.toString();
                if (StrUtil.isNotBlank(json)) {
                    return JSONUtil.toList(json, clazz);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 设置Hash缓存
     *
     * @param key           Hash键
     * @param field         字段名
     * @param value         缓存值
     * @param expireMinutes 过期时间（分钟）
     * @param <T>           泛型类型
     */
    public <T> void hSet(String key, String field, T value, int expireMinutes) {
        try {
            String json = JSONUtil.toJsonStr(value);
            stringRedisTemplate.opsForHash().put(key, field, json);
            stringRedisTemplate.expire(key, expireMinutes, TimeUnit.MINUTES);
        } catch (Exception e) {
            // 静默处理异常
        }
    }

    /**
     * 从Hash缓存获取对象，如果不存在则计算并缓存
     *
     * @param key           Hash键
     * @param field         字段名
     * @param clazz         对象类型
     * @param supplier      数据提供者
     * @param expireMinutes 过期时间（分钟）
     * @param <T>           泛型类型
     * @return 缓存的对象或计算后的对象
     */
    public <T> T hGetOrCompute(String key, String field, Class<T> clazz, Supplier<T> supplier, int expireMinutes) {
        T cached = hGet(key, field, clazz);
        if (cached != null) {
            return cached;
        }

        T computed = supplier.get();
        if (computed != null) {
            hSet(key, field, computed, expireMinutes);
        }
        return computed;
    }

    /**
     * 从Hash缓存获取列表，如果不存在则计算并缓存
     *
     * @param key           Hash键
     * @param field         字段名
     * @param clazz         列表元素类型
     * @param supplier      数据提供者
     * @param expireMinutes 过期时间（分钟）
     * @param <T>           泛型类型
     * @return 缓存的列表或计算后的列表
     */
    public <T> List<T> hGetListOrCompute(String key, String field, Class<T> clazz, Supplier<List<T>> supplier, int expireMinutes) {
        List<T> cached = hGetList(key, field, clazz);
        if (cached != null) {
            return cached;
        }

        List<T> computed = supplier.get();
        if (computed != null) {
            hSet(key, field, computed, expireMinutes);
        }
        return computed;
    }

    /**
     * 删除Hash中的指定字段
     *
     * @param key   Hash键
     * @param field 字段名
     */
    public void hDelete(String key, String field) {
        try {
            stringRedisTemplate.opsForHash().delete(key, field);
        } catch (Exception e) {
            // 静默处理异常
        }
    }

    /**
     * 删除整个Hash缓存
     *
     * @param key Hash键
     */
    public void hDeleteAll(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            // 静默处理异常
        }
    }

    /**
     * 获取Hash的所有字段名
     *
     * @param key Hash键
     * @return 字段名集合
     */
    public Set<String> hKeys(String key) {
        try {
            Set<Object> keys = stringRedisTemplate.opsForHash().keys(key);
            return keys.stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            return Set.of();
        }
    }

    // ==================== Set结构操作 ====================

    /**
     * 向Set中添加元素
     *
     * @param key    Set键
     * @param members 元素集合
     */
    public void sAdd(String key, String... members) {
        try {
            stringRedisTemplate.opsForSet().add(key, members);
        } catch (Exception e) {
            // 静默处理异常
        }
    }

    /**
     * 从Set中移除元素
     *
     * @param key    Set键
     * @param members 元素集合
     */
    public void sRem(String key, String... members) {
        try {
            stringRedisTemplate.opsForSet().remove(key, (Object[]) members);
        } catch (Exception e) {
            // 静默处理异常
        }
    }

    /**
     * 获取Set中的所有元素
     *
     * @param key Set键
     * @return 元素集合
     */
    public Set<String> sMembers(String key) {
        try {
            return stringRedisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            return Set.of();
        }
    }

    /**
     * 删除整个Set
     *
     * @param key Set键
     */
    public void sDelete(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            // 静默处理异常
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
        }
        return computed;
    }
}
