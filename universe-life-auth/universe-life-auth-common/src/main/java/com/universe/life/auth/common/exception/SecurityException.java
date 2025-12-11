package com.universe.life.auth.common.exception;

/**
 * 安全相关异常
 * 用于处理速率限制、暴力破解防护等安全场景
 *
 * @author Quinn (Test Architect)
 * @since 2025/11/17
 */
public class SecurityException extends RuntimeException {

    public static class RateLimitExceededException extends SecurityException {
        public RateLimitExceededException(String message) {
            super(message);
        }
    }

    public static class AccountLockedException extends SecurityException {
        public AccountLockedException(String message) {
            super(message);
        }
    }

    public static class CaptchaRateLimitExceededException extends SecurityException {
        public CaptchaRateLimitExceededException(String message) {
            super(message);
        }
    }

    public static class CaptchaVerificationFailedException extends SecurityException {
        public CaptchaVerificationFailedException(String message) {
            super(message);
        }
    }

    public static class InvalidCredentialsException extends SecurityException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }

    public SecurityException(String message) {
        super(message);
    }

    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}