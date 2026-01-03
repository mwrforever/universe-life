package com.universe.life.auth.common.exception;

import com.universe.life.auth.common.domain.Result;
import lombok.Getter;

/**
 * 数据库异常类
 *
 * @author universe-life
 */
@Getter
public class DatabaseException extends BaseServiceException {

    public DatabaseException(String message) {
        super(ErrorCode.DATABASE_ERROR, message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(ErrorCode.DATABASE_ERROR, message, cause);
    }

    public DatabaseException(int code, String message) {
        super(code, message);
    }

    public DatabaseException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

    @Override
    public Result<Void> toResult() {
        return Result.error(this.message);
    }

    /**
     * 数据库连接异常
     */
    public static class ConnectionException extends DatabaseException {
        public ConnectionException(String message) {
            super(ErrorCode.DATABASE_CONNECTION_ERROR, message);
        }

        public ConnectionException(String message, Throwable cause) {
            super(ErrorCode.DATABASE_CONNECTION_ERROR, message, cause);
        }
    }

    /**
     * 数据库查询异常
     */
    public static class QueryException extends DatabaseException {
        public QueryException(String message) {
            super(ErrorCode.DATABASE_QUERY_ERROR, message);
        }

        public QueryException(String message, Throwable cause) {
            super(ErrorCode.DATABASE_QUERY_ERROR, message, cause);
        }
    }

    /**
     * 数据库更新异常
     */
    public static class UpdateException extends DatabaseException {
        public UpdateException(String message) {
            super(ErrorCode.DATABASE_UPDATE_ERROR, message);
        }

        public UpdateException(String message, Throwable cause) {
            super(ErrorCode.DATABASE_UPDATE_ERROR, message, cause);
        }
    }

    /**
     * 数据库事务异常
     */
    public static class TransactionException extends DatabaseException {
        public TransactionException(String message) {
            super(ErrorCode.DATABASE_TRANSACTION_ERROR, message);
        }

        public TransactionException(String message, Throwable cause) {
            super(ErrorCode.DATABASE_TRANSACTION_ERROR, message, cause);
        }
    }

    /**
     * 数据库约束违反异常
     */
    public static class DatabaseConstraintViolationException extends DatabaseException {
        public DatabaseConstraintViolationException(String message) {
            super(ErrorCode.DATABASE_CONSTRAINT_VIOLATION, message);
        }

        public DatabaseConstraintViolationException(String message, Throwable cause) {
            super(ErrorCode.DATABASE_CONSTRAINT_VIOLATION, message, cause);
        }
    }
}