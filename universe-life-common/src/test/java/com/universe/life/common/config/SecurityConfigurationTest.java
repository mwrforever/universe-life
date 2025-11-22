package com.universe.life.common.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SecurityConfiguration 单元测试
 * 测试速率限制和暴力破解防护功能
 *
 * @author Quinn (Test Architect)
 * @since 2025/11/17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("安全配置测试")
class SecurityConfigurationTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private RedisScript<Object> rateLimitScript;

    private SecurityConfiguration securityConfig;
    private SecurityConfiguration.RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfiguration();

        // 设置测试配置
        SecurityConfiguration.RateLimit rateLimit = new SecurityConfiguration.RateLimit();
        rateLimit.setLoginAttempts(5);
        rateLimit.setTimeWindowSeconds(60);
        rateLimit.setCaptchaRequests(3);
        rateLimit.setCaptchaTimeWindowSeconds(300);
        securityConfig.setRateLimit(rateLimit);

        SecurityConfiguration.BruteForceProtection bruteForce = new SecurityConfiguration.BruteForceProtection();
        bruteForce.setMaxAttempts(5);
        bruteForce.setLockoutDurationMinutes(30);
        bruteForce.setEnabled(true);
        securityConfig.setBruteForceProtection(bruteForce);

        rateLimitService = new SecurityConfiguration.RateLimitService(
                redisTemplate, rateLimitScript, securityConfig);
    }

    @Test
    @DisplayName("登录速率限制 - 允许正常请求")
    void testCheckLoginRateLimit_Allowed() {
        // Given
        String identifier = "test@example.com";
        when(redisTemplate.execute(any(RedisScript.class), any(List.class), anyString(), anyString(), anyString()))
                .thenReturn(List.of(1, 4)); // 允许，剩余4次

        // When
        boolean result = rateLimitService.checkLoginRateLimit(identifier);

        // Then
        assertTrue(result);
        verify(redisTemplate).execute(eq(rateLimitScript), eq(Collections.singletonList("rate_limit:login:" + identifier)),
                eq("60"), eq("5"), anyString());
    }

    @Test
    @DisplayName("登录速率限制 - 超过限制")
    void testCheckLoginRateLimit_Exceeded() {
        // Given
        String identifier = "test@example.com";
        when(redisTemplate.execute(any(RedisScript.class), any(List.class), anyString(), anyString(), anyString()))
                .thenReturn(List.of(0, 0)); // 超过限制

        // When
        boolean result = rateLimitService.checkLoginRateLimit(identifier);

        // Then
        assertFalse(result);
        verify(redisTemplate).execute(eq(rateLimitScript), eq(Collections.singletonList("rate_limit:login:" + identifier)),
                eq("60"), eq("5"), anyString());
    }

    @Test
    @DisplayName("验证码速率限制 - 允许正常请求")
    void testCheckCaptchaRateLimit_Allowed() {
        // Given
        String identifier = "test@example.com";
        when(redisTemplate.execute(any(RedisScript.class), any(List.class), anyString(), anyString(), anyString()))
                .thenReturn(List.of(1, 2)); // 允许，剩余2次

        // When
        boolean result = rateLimitService.checkCaptchaRateLimit(identifier);

        // Then
        assertTrue(result);
        verify(redisTemplate).execute(eq(rateLimitScript), eq(Collections.singletonList("rate_limit:captcha:" + identifier)),
                eq("300"), eq("3"), anyString());
    }

    @Test
    @DisplayName("验证码速率限制 - 超过限制")
    void testCheckCaptchaRateLimit_Exceeded() {
        // Given
        String identifier = "test@example.com";
        when(redisTemplate.execute(any(RedisScript.class), any(List.class), anyString(), anyString(), anyString()))
                .thenReturn(List.of(0, 0)); // 超过限制

        // When
        boolean result = rateLimitService.checkCaptchaRateLimit(identifier);

        // Then
        assertFalse(result);
        verify(redisTemplate).execute(eq(rateLimitScript), eq(Collections.singletonList("rate_limit:captcha:" + identifier)),
                eq("300"), eq("3"), anyString());
    }

    @Test
    @DisplayName("记录登录失败 - 未超过最大失败次数")
    void testRecordLoginFailure_NotExceeded() {
        // Given
        String identifier = "test@example.com";
        when(redisTemplate.opsForValue().increment(anyString())).thenReturn(3L); // 小于最大失败次数

        // When
        boolean locked = rateLimitService.recordLoginFailure(identifier);

        // Then
        assertFalse(locked);
        verify(redisTemplate).opsForValue().increment("auth:failed_count:" + identifier);
        verify(redisTemplate).expire(eq("auth:failed_count:" + identifier), any());
        verify(redisTemplate, never()).opsForValue().set(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("记录登录失败 - 达到最大失败次数，账户被锁定")
    void testRecordLoginFailure_ExceededAndLocked() {
        // Given
        String identifier = "test@example.com";
        when(redisTemplate.opsForValue().increment(anyString())).thenReturn(5L); // 达到最大失败次数

        // When
        boolean locked = rateLimitService.recordLoginFailure(identifier);

        // Then
        assertTrue(locked);
        verify(redisTemplate).opsForValue().increment("auth:failed_count:" + identifier);
        verify(redisTemplate).opsForValue().set(eq("auth:locked:" + identifier), eq("true"), any());
    }

    @Test
    @DisplayName("检查账户锁定状态 - 账户被锁定")
    void testIsAccountLocked_True() {
        // Given
        String identifier = "test@example.com";
        when(redisTemplate.hasKey("auth:locked:" + identifier)).thenReturn(true);

        // When
        boolean locked = rateLimitService.isAccountLocked(identifier);

        // Then
        assertTrue(locked);
        verify(redisTemplate).hasKey("auth:locked:" + identifier);
    }

    @Test
    @DisplayName("检查账户锁定状态 - 账户未锁定")
    void testIsAccountLocked_False() {
        // Given
        String identifier = "test@example.com";
        when(redisTemplate.hasKey("auth:locked:" + identifier)).thenReturn(false);

        // When
        boolean locked = rateLimitService.isAccountLocked(identifier);

        // Then
        assertFalse(locked);
        verify(redisTemplate).hasKey("auth:locked:" + identifier);
    }

    @Test
    @DisplayName("清除登录失败记录")
    void testClearLoginFailures() {
        // Given
        String identifier = "test@example.com";

        // When
        rateLimitService.clearLoginFailures(identifier);

        // Then
        verify(redisTemplate).delete("auth:failed_count:" + identifier);
        verify(redisTemplate).delete("auth:locked:" + identifier);
    }

    @Test
    @DisplayName("暴力破解防护禁用时的行为")
    void testBruteForceProtectionDisabled() {
        // Given - 禁用暴力破解防护
        securityConfig.getBruteForceProtection().setEnabled(false);
        rateLimitService = new SecurityConfiguration.RateLimitService(
                redisTemplate, rateLimitScript, securityConfig);

        // When & Then
        assertFalse(rateLimitService.recordLoginFailure("test@example.com"));
        assertFalse(rateLimitService.isAccountLocked("test@example.com"));

        // 验证没有调用Redis操作
        verify(redisTemplate, never()).opsForValue();
        verify(redisTemplate, never()).hasKey(anyString());
    }

    @Test
    @DisplayName("首次登录失败设置过期时间")
    void testFirstLoginFailureSetsExpiration() {
        // Given
        String identifier = "test@example.com";
        when(redisTemplate.opsForValue().increment(anyString())).thenReturn(1L); // 首次失败

        // When
        rateLimitService.recordLoginFailure(identifier);

        // Then
        verify(redisTemplate).opsForValue().increment("auth:failed_count:" + identifier);
        verify(redisTemplate).expire(eq("auth:failed_count:" + identifier), any());
        verify(redisTemplate, never()).opsForValue().set(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Redis脚本执行异常处理")
    void testRedisScriptExecutionException() {
        // Given
        String identifier = "test@example.com";
        when(redisTemplate.execute(any(RedisScript.class), any(List.class), anyString(), anyString(), anyString()))
                .thenReturn(null); // 返回null模拟异常情况

        // When
        boolean result = rateLimitService.checkLoginRateLimit(identifier);

        // Then
        assertFalse(result); // 异常情况下应该拒绝请求
    }
}