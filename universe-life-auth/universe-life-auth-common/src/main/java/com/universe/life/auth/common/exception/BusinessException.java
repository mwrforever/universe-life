package com.universe.life.auth.common.exception;

import com.universe.life.auth.common.domain.Result;
import lombok.Getter;

/**
 * 业务异常类
 *
 * @author universe-life
 */
@Getter
public class BusinessException extends BaseServiceException {

    public BusinessException(String message) {
        super(ErrorCode.BUSINESS_ERROR, message);
    }

    public BusinessException(String message, Throwable cause) {
        super(ErrorCode.BUSINESS_ERROR, message, cause);
    }

    public BusinessException(int code, String message) {
        super(code, message);
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

    @Override
    public Result<Void> toResult() {
        return Result.error(this.code, this.message);
    }

    /**
     * 参数错误异常
     */
    public static class ParamException extends BusinessException {
        public ParamException(String message) {
            super(ErrorCode.PARAM_ERROR, message);
        }

        public ParamException(String message, Throwable cause) {
            super(ErrorCode.PARAM_ERROR, message, cause);
        }
    }

    /**
     * 数据未找到异常
     */
    public static class DataNotFoundException extends BusinessException {
        public DataNotFoundException(String message) {
            super(ErrorCode.DATA_NOT_FOUND, message);
        }

        public DataNotFoundException(String message, Throwable cause) {
            super(ErrorCode.DATA_NOT_FOUND, message, cause);
        }
    }

    public static class OperationFailedException extends BusinessException {
        public OperationFailedException(String message) {
            super(message);
        }

        public OperationFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 数据已存在异常
     */
    public static class DataAlreadyExistsException extends BusinessException {
        public DataAlreadyExistsException(String message) {
            super(ErrorCode.DATA_ALREADY_EXISTS, message);
        }

        public DataAlreadyExistsException(String message, Throwable cause) {
            super(ErrorCode.DATA_ALREADY_EXISTS, message, cause);
        }
    }

    /**
     * 操作不允许异常
     */
    public static class OperationNotAllowedException extends BusinessException {
        public OperationNotAllowedException(String message) {
            super(ErrorCode.OPERATION_NOT_ALLOWED, message);
        }

        public OperationNotAllowedException(String message, Throwable cause) {
            super(ErrorCode.OPERATION_NOT_ALLOWED, message, cause);
        }
    }


}