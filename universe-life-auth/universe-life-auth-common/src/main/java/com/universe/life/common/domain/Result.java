package com.universe.life.common.domain;

/**
 * 统一响应结果封装类
 * 替代原有的ErrorResponse，提供更完整的响应功能
 *
 * @author 毛伟然
 * @since 2025/11/3 15:51
 */
public record Result<T>(
        Integer code,
        String message,
        T data,
        Long timestamp
) {
    public static <T> Result<T> success(T data) {
        return new Result<>(0, "success", data, System.currentTimeMillis());
    }

    public static <T> Result<T> success() {
        return new Result<>(0, "success", null, System.currentTimeMillis());
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(1, message, null, System.currentTimeMillis());
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null, System.currentTimeMillis());
    }

    public static <T> Result<T> error(Integer code, String message, T data) {
        return new Result<>(code, message, data, System.currentTimeMillis());
    }

    // 为了兼容性，保留原有的构造方法
    public Result(Integer code, String message, T data) {
        this(code, message, data, System.currentTimeMillis());
    }
}
