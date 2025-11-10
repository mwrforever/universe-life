package com.universe.life.auth.resource.handler;

import com.universe.life.common.domain.Result;
import com.universe.life.common.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一处理应用程序中的各种异常
 * 注意：JWT相关的认证异常已由JwtAccessDeniedHandler和JwtAuthenticationExceptionHandler处理，此处不再重复处理
 *
 * @author universe-life
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理基础服务异常
     */
    @ExceptionHandler(BaseServiceException.class)
    public ResponseEntity<Result<Void>> handleBaseServiceException(BaseServiceException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.toResult());
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.toResult());
    }

    /**
     * 处理数据库异常
     */
    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<Result<Void>> handleDatabaseException(DatabaseException e) {
        log.error("数据库异常: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.toResult());
    }

    /**
     * 处理网络异常
     */
    @ExceptionHandler(NetworkException.class)
    public ResponseEntity<Result<Void>> handleNetworkException(NetworkException e) {
        log.error("网络异常: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(e.toResult());
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<Result<Void>> handleSystemException(SystemException e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.toResult());
    }

    /**
     * 处理 Spring Security AuthenticationException
     * 注意：此类异常通常由JwtAuthenticationExceptionHandler处理，此处作为兜底
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<Void>> handleAuthenticationException(AuthenticationException e) {
        log.error("Spring Security认证异常: {}", e.getMessage(), e);

        // 判断是否是我们的自定义认证异常
        if (e instanceof AuthException.AuthenticationException authException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(authException.toResult());
        }

        // 对于其他Spring Security认证异常，使用通用响应
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(HttpStatus.UNAUTHORIZED.value(), "认证失败: " + e.getMessage()));
    }

    /**
     * 处理 Spring Security AccessDeniedException
     * 注意：此类异常通常由JwtAccessDeniedHandler处理，此处作为兜底
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.error("Spring Security授权异常: {}", e.getMessage(), e);

        // 判断是否是我们的自定义授权异常
        if (e instanceof AuthException.AuthorizationException authException) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(authException.toResult());
        }

        // 对于其他Spring Security授权异常，使用通用响应
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.error(HttpStatus.FORBIDDEN.value(), "权限不足: " + e.getMessage()));
    }

    /**
     * 处理 IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("参数非法异常: {}", e.getMessage(), e);
        BusinessException.ParamException paramException = new BusinessException.ParamException(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(paramException.toResult());
    }

    /**
     * 处理 RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Void>> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        SystemException systemException = new SystemException("系统运行时错误: " + e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(systemException.toResult());
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("未知异常: {}", e.getMessage(), e);
        SystemException systemException = new SystemException("系统内部错误", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(systemException.toResult());
    }
}