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

    public BaseServiceException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BaseServiceException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public BaseServiceException(String message) {
        this.code = 0;
        this.message = message;
    }

    /**
     * 转换为错误响应对象
     *
     * @return Result
     */
    public abstract Result<Void> toResult();

    @Override
    public String toString() {
        return String.format("%s{code=%d, message='%s'}",
                this.getClass().getSimpleName(), code, message);
    }
}