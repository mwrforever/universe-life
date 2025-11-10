package com.universe.life.common.exception;

import com.universe.life.common.domain.Result;
import com.universe.life.common.message.ExceptionMessage;
import lombok.Getter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * 认证授权异常类
 * 继承Spring Security的标准异常类，提供更完整的认证授权功能
 *
 * @author universe-life
 */
@Getter
public class AuthException {

    /**
     * 基础认证异常
     */
    public static class AuthenticationException extends AuthenticationServiceException {
        private final int code;
        private final long timestamp;

        /**
         * 默认构造方法 - 使用标准的HTTP 401错误码和消息
         */
        public AuthenticationException() {
            this(ErrorCode.UNAUTHORIZED, ExceptionMessage.AUTH_FAILED);
        }

        public AuthenticationException(int code, String message) {
            super(message);
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        public AuthenticationException(int code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * 只提供消息的构造方法 - 使用默认HTTP 401错误码
         */
        public AuthenticationException(String message) {
            this(ErrorCode.UNAUTHORIZED, message);
        }

        /**
         * 只提供异常原因的构造方法 - 使用默认HTTP 401错误码和消息
         */
        public AuthenticationException(Throwable cause) {
            this(ErrorCode.UNAUTHORIZED, ExceptionMessage.AUTH_FAILED, cause);
        }

        public Result<Void> toResult() {
            return Result.error(this.code, this.getMessage());
        }
    }

    /**
     * 授权失败异常 - 继承Spring Security的AccessDeniedException
     */
    public static class AuthorizationException extends AccessDeniedException {
        private final int code;
        private final long timestamp;

        /**
         * 默认构造方法 - 使用标准的HTTP 403错误码和消息
         */
        public AuthorizationException() {
            this(ErrorCode.FORBIDDEN, ExceptionMessage.ACCESS_DENIED);
        }

        public AuthorizationException(int code, String message) {
            super(message);
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        public AuthorizationException(int code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * 只提供消息的构造方法 - 使用默认HTTP 403错误码
         */
        public AuthorizationException(String message) {
            this(ErrorCode.FORBIDDEN, message);
        }

        /**
         * 只提供异常原因的构造方法 - 使用默认HTTP 403错误码和消息
         */
        public AuthorizationException(Throwable cause) {
            this(ErrorCode.FORBIDDEN, ExceptionMessage.ACCESS_DENIED, cause);
        }

        public Result<Void> toResult() {
            return Result.error(this.code, this.getMessage());
        }
    }

    /**
     * Token过期异常 - 继承Spring Security的CredentialsExpiredException
     */
    public static class TokenExpiredException extends CredentialsExpiredException {
        private final int code;
        private final long timestamp;

        /**
         * 默认构造方法 - 使用标准的HTTP 401错误码和消息
         */
        public TokenExpiredException() {
            this(ErrorCode.UNAUTHORIZED, ExceptionMessage.AUTH_TOKEN_EXPIRED);
        }

        public TokenExpiredException(int code, String message) {
            super(message);
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        public TokenExpiredException(int code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * 只提供消息的构造方法 - 使用默认HTTP 401错误码
         */
        public TokenExpiredException(String message) {
            this(ErrorCode.UNAUTHORIZED, message);
        }

        /**
         * 只提供异常原因的构造方法 - 使用默认HTTP 401错误码和消息
         */
        public TokenExpiredException(Throwable cause) {
            this(ErrorCode.UNAUTHORIZED, ExceptionMessage.AUTH_TOKEN_EXPIRED, cause);
        }

        public Result<Void> toResult() {
            return Result.error(this.code, this.getMessage());
        }
    }

    /**
     * Token无效异常 - 继承Spring Security的BadCredentialsException
     */
    public static class TokenInvalidException extends BadCredentialsException {
        private final int code;
        private final long timestamp;

        /**
         * 默认构造方法 - 使用标准的HTTP 401错误码和消息
         */
        public TokenInvalidException() {
            this(ErrorCode.UNAUTHORIZED, ExceptionMessage.AUTH_TOKEN_INVALID);
        }

        public TokenInvalidException(int code, String message) {
            super(message);
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        public TokenInvalidException(int code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * 只提供消息的构造方法 - 使用默认HTTP 401错误码
         */
        public TokenInvalidException(String message) {
            this(ErrorCode.UNAUTHORIZED, message);
        }

        /**
         * 只提供异常原因的构造方法 - 使用默认HTTP 401错误码和消息
         */
        public TokenInvalidException(Throwable cause) {
            this(ErrorCode.UNAUTHORIZED, ExceptionMessage.AUTH_TOKEN_INVALID, cause);
        }

        public Result<Void> toResult() {
            return Result.error(this.code, this.getMessage());
        }
    }

    /**
     * Token缺失异常 - 继承Spring Security的AuthenticationServiceException
     */
    public static class TokenMissingException extends AuthenticationServiceException {
        private final int code;
        private final long timestamp;

        /**
         * 默认构造方法 - 使用标准的HTTP 401错误码和消息
         */
        public TokenMissingException() {
            this(ErrorCode.UNAUTHORIZED, ExceptionMessage.AUTH_TOKEN_MISSING);
        }

        public TokenMissingException(int code, String message) {
            super(message);
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        public TokenMissingException(int code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * 只提供消息的构造方法 - 使用默认HTTP 401错误码
         */
        public TokenMissingException(String message) {
            this(ErrorCode.UNAUTHORIZED, message);
        }

        /**
         * 只提供异常原因的构造方法 - 使用默认HTTP 401错误码和消息
         */
        public TokenMissingException(Throwable cause) {
            this(ErrorCode.UNAUTHORIZED, ExceptionMessage.AUTH_TOKEN_MISSING, cause);
        }

        public Result<Void> toResult() {
            return Result.error(this.code, this.getMessage());
        }
    }

    /**
     * 用户未找到异常 - 继承Spring Security的UsernameNotFoundException
     */
    public static class UserNotFoundException extends UsernameNotFoundException {
        private final int code;
        private final long timestamp;

        /**
         * 默认构造方法 - 使用标准的HTTP 401错误码和消息
         */
        public UserNotFoundException() {
            this(ErrorCode.UNAUTHORIZED, ExceptionMessage.USER_NOT_FOUND);
        }

        public UserNotFoundException(int code, String message) {
            super(message);
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        public UserNotFoundException(int code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * 只提供消息的构造方法 - 使用默认HTTP 401错误码
         */
        public UserNotFoundException(String message) {
            this(ErrorCode.UNAUTHORIZED, message);
        }

        /**
         * 只提供异常原因的构造方法 - 使用默认HTTP 401错误码和消息
         */
        public UserNotFoundException(Throwable cause) {
            this(ErrorCode.UNAUTHORIZED, ExceptionMessage.USER_NOT_FOUND, cause);
        }

        public Result<Void> toResult() {
            return Result.error(this.code, this.getMessage());
        }
    }

    /**
     * 密码错误异常 - 继承Spring Security的BadCredentialsException
     */
    public static class PasswordIncorrectException extends BadCredentialsException {
        private final int code;
        private final long timestamp;

        /**
         * 默认构造方法 - 使用标准的HTTP 401错误码和消息
         */
        public PasswordIncorrectException() {
            this(ErrorCode.UNAUTHORIZED, ExceptionMessage.PASSWORD_INCORRECT);
        }

        public PasswordIncorrectException(int code, String message) {
            super(message);
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        public PasswordIncorrectException(int code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * 只提供消息的构造方法 - 使用默认HTTP 401错误码
         */
        public PasswordIncorrectException(String message) {
            this(ErrorCode.UNAUTHORIZED, message);
        }

        /**
         * 只提供异常原因的构造方法 - 使用默认HTTP 401错误码和消息
         */
        public PasswordIncorrectException(Throwable cause) {
            this(ErrorCode.UNAUTHORIZED, ExceptionMessage.PASSWORD_INCORRECT, cause);
        }

        public Result<Void> toResult() {
            return Result.error(this.code, this.getMessage());
        }
    }

    /**
     * 账户锁定异常 - 继承Spring Security的LockedException
     */
    public static class AccountLockedException extends LockedException {
        private final int code;
        private final long timestamp;

        /**
         * 默认构造方法 - 使用标准的HTTP 403错误码和消息
         */
        public AccountLockedException() {
            this(ErrorCode.FORBIDDEN, ExceptionMessage.ACCOUNT_LOCKED);
        }

        public AccountLockedException(int code, String message) {
            super(message);
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        public AccountLockedException(int code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * 只提供消息的构造方法 - 使用默认HTTP 403错误码
         */
        public AccountLockedException(String message) {
            this(ErrorCode.FORBIDDEN, message);
        }

        /**
         * 只提供异常原因的构造方法 - 使用默认HTTP 403错误码和消息
         */
        public AccountLockedException(Throwable cause) {
            this(ErrorCode.FORBIDDEN, ExceptionMessage.ACCOUNT_LOCKED, cause);
        }

        public Result<Void> toResult() {
            return Result.error(this.code, this.getMessage());
        }
    }

    /**
     * 账户禁用异常 - 继承Spring Security的DisabledException
     */
    public static class AccountDisabledException extends DisabledException {
        private final int code;
        private final long timestamp;

        /**
         * 默认构造方法 - 使用标准的HTTP 403错误码和消息
         */
        public AccountDisabledException() {
            this(ErrorCode.FORBIDDEN, ExceptionMessage.ACCOUNT_DISABLED);
        }

        public AccountDisabledException(int code, String message) {
            super(message);
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        public AccountDisabledException(int code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * 只提供消息的构造方法 - 使用默认HTTP 403错误码
         */
        public AccountDisabledException(String message) {
            this(ErrorCode.FORBIDDEN, message);
        }

        /**
         * 只提供异常原因的构造方法 - 使用默认HTTP 403错误码和消息
         */
        public AccountDisabledException(Throwable cause) {
            this(ErrorCode.FORBIDDEN, ExceptionMessage.ACCOUNT_DISABLED, cause);
        }

        public Result<Void> toResult() {
            return Result.error(this.code, this.getMessage());
        }
    }

    /**
     * 账户过期异常 - 继承Spring Security的AccountExpiredException
     * 避免命名冲突，使用不同的类名
     */
    public static class UserAccountExpiredException extends AccountExpiredException {
        private final int code;
        private final long timestamp;

        /**
         * 默认构造方法 - 使用标准的HTTP 403错误码和消息
         */
        public UserAccountExpiredException() {
            this(ErrorCode.FORBIDDEN, ExceptionMessage.ACCOUNT_EXPIRED);
        }

        public UserAccountExpiredException(int code, String message) {
            super(message);
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        public UserAccountExpiredException(int code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * 只提供消息的构造方法 - 使用默认HTTP 403错误码
         */
        public UserAccountExpiredException(String message) {
            this(ErrorCode.FORBIDDEN, message);
        }

        /**
         * 只提供异常原因的构造方法 - 使用默认HTTP 403错误码和消息
         */
        public UserAccountExpiredException(Throwable cause) {
            this(ErrorCode.FORBIDDEN, ExceptionMessage.ACCOUNT_EXPIRED, cause);
        }

        public Result<Void> toResult() {
            return Result.error(this.code, this.getMessage());
        }
    }
}