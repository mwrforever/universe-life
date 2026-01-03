package com.universe.life.auth.common.domain;

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
        T data
) {
    public static <T> Result<T> success(T data) {
        return new Result<>(1, "success", data);
    }

    public static <T> Result<T> success() {
        return new Result<>(1, "success", null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(0, message, null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }
}
