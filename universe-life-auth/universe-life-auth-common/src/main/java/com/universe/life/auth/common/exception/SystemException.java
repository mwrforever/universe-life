package com.universe.life.auth.common.exception;

import com.universe.life.auth.common.domain.Result;
import lombok.Getter;

/**
 * 系统异常类
 *
 * @author universe-life
 */
@Getter
public class SystemException extends BaseServiceException {

    public SystemException(String message) {
        super(ErrorCode.SYSTEM_ERROR, message);
    }

    public SystemException(String message, Throwable cause) {
        super(ErrorCode.SYSTEM_ERROR, message, cause);
    }

    public SystemException(int code, String message) {
        super(code, message);
    }

    public SystemException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

    @Override
    public Result<Void> toResult() {
        return Result.error(this.code, this.message);
    }

    /**
     * 配置错误异常
     */
    public static class ConfigException extends SystemException {
        public ConfigException(String message) {
            super(ErrorCode.CONFIG_ERROR, message);
        }

        public ConfigException(String message, Throwable cause) {
            super(ErrorCode.CONFIG_ERROR, message, cause);
        }
    }

    /**
     * 文件未找到异常
     */
    public static class SystemFileNotFoundException extends SystemException {
        public SystemFileNotFoundException(String message) {
            super(ErrorCode.FILE_NOT_FOUND, message);
        }

        public SystemFileNotFoundException(String message, Throwable cause) {
            super(ErrorCode.FILE_NOT_FOUND, message, cause);
        }
    }

    /**
     * 文件读取异常
     */
    public static class FileReadException extends SystemException {
        public FileReadException(String message) {
            super(ErrorCode.FILE_READ_ERROR, message);
        }

        public FileReadException(String message, Throwable cause) {
            super(ErrorCode.FILE_READ_ERROR, message, cause);
        }
    }

    /**
     * 文件写入异常
     */
    public static class FileWriteException extends SystemException {
        public FileWriteException(String message) {
            super(ErrorCode.FILE_WRITE_ERROR, message);
        }

        public FileWriteException(String message, Throwable cause) {
            super(ErrorCode.FILE_WRITE_ERROR, message, cause);
        }
    }

    /**
     * 内存错误异常
     */
    public static class MemoryException extends SystemException {
        public MemoryException(String message) {
            super(ErrorCode.MEMORY_ERROR, message);
        }

        public MemoryException(String message, Throwable cause) {
            super(ErrorCode.MEMORY_ERROR, message, cause);
        }
    }
}