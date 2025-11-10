package com.universe.life.common.exception;

import com.universe.life.common.domain.Result;
import lombok.Getter;

/**
 * 网络异常类
 *
 * @author universe-life
 */
@Getter
public class NetworkException extends BaseServiceException {

    public NetworkException(String message) {
        super(ErrorCode.NETWORK_ERROR, message);
    }

    public NetworkException(String message, Throwable cause) {
        super(ErrorCode.NETWORK_ERROR, message, cause);
    }

    public NetworkException(int code, String message) {
        super(code, message);
    }

    public NetworkException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

    @Override
    public Result<Void> toResult() {
        return Result.error(this.code, this.message);
    }

    /**
     * 连接超时异常
     */
    public static class ConnectionTimeoutException extends NetworkException {
        public ConnectionTimeoutException(String message) {
            super(ErrorCode.CONNECTION_TIMEOUT, message);
        }

        public ConnectionTimeoutException(String message, Throwable cause) {
            super(ErrorCode.CONNECTION_TIMEOUT, message, cause);
        }
    }

    /**
     * 连接被拒绝异常
     */
    public static class ConnectionRefusedException extends NetworkException {
        public ConnectionRefusedException(String message) {
            super(ErrorCode.CONNECTION_REFUSED, message);
        }

        public ConnectionRefusedException(String message, Throwable cause) {
            super(ErrorCode.CONNECTION_REFUSED, message, cause);
        }
    }

    /**
     * 服务不可用异常
     */
    public static class ServiceUnavailableException extends NetworkException {
        public ServiceUnavailableException(String message) {
            super(ErrorCode.SERVICE_UNAVAILABLE_ERROR, message);
        }

        public ServiceUnavailableException(String message, Throwable cause) {
            super(ErrorCode.SERVICE_UNAVAILABLE_ERROR, message, cause);
        }
    }
}