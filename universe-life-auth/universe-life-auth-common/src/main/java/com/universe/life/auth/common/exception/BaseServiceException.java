package com.universe.life.auth.common.exception;

import com.universe.life.auth.common.domain.Result;
import lombok.Getter;

/**
 * 基础服务异常类
 * 所有业务异常类的父类
 *
 * @author universe-life
 */
@Getter
public abstract class BaseServiceException extends RuntimeException {

    /**
     * 错误码
     */
    protected final int code;

    /**
     * 错误消息
     */
    protected final String message;

    /**
     * 时间戳
     */
    protected final long timestamp;

    public BaseServiceException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public BaseServiceException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 转换为错误响应对象
     *
     * @return Result
     */
    public abstract Result<Void> toResult();

    @Override
    public String toString() {
        return String.format("%s{code=%d, message='%s', timestamp=%d}",
                this.getClass().getSimpleName(), code, message, timestamp);
    }
}