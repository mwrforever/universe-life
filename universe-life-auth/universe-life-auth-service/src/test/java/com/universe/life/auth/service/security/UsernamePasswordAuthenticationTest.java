package com.universe.life.auth.service.security;

import com.universe.life.auth.service.security.filter.UsernamePasswordAuthenticationFilter;
import com.universe.life.auth.service.security.provider.UsernamePasswordAuthenticationProvider;
import com.universe.life.auth.service.security.token.UsernamePasswordAuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 用户名密码认证测试
 *
 * @author 毛伟然
 * @since 2025/11/21
 */
@DisplayName("用户名密码认证测试")
class UsernamePasswordAuthenticationTest {

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    private UsernamePasswordAuthenticationProvider authenticationProvider;
    private UsernamePasswordAuthenticationFilter authenticationFilter;

    @BeforeEach
    void setUp() {
        authenticationProvider = new UsernamePasswordAuthenticationProvider(userDetailsService, passwordEncoder);
        authenticationFilter = new UsernamePasswordAuthenticationFilter(authenticationManager);
    }

    @Test
    @DisplayName("认证提供者 - 成功认证")
    void testAuthenticationProvider_Success() {
        // 准备测试数据
        String username = "testuser";
        String password = "testpass";
        String encodedPassword = "$2a$10$encodedPassword";

        UserDetails userDetails = User.builder()
                .username(username)
                .password(encodedPassword)
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        // Mock依赖
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        // 创建认证令牌
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(username, password);

        // 执行认证
        var result = authenticationProvider.authenticate(authToken);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        assertEquals(userDetails, result.getPrincipal());
        assertEquals(1, result.getAuthorities().size());

        // 验证依赖调用
        verify(userDetailsService).loadUserByUsername(username);
        verify(passwordEncoder).matches(password, encodedPassword);
    }

    @Test
    @DisplayName("认证提供者 - 密码错误")
    void testAuthenticationProvider_WrongPassword() {
        // 准备测试数据
        String username = "testuser";
        String password = "wrongpass";
        String encodedPassword = "$2a$10$encodedPassword";

        UserDetails userDetails = User.builder()
                .username(username)
                .password(encodedPassword)
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        // Mock依赖
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        // 创建认证令牌
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(username, password);

        // 执行认证并验证异常
        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
            () -> authenticationProvider.authenticate(authToken));

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("认证提供者 - 用户不存在")
    void testAuthenticationProvider_UserNotFound() {
        // 准备测试数据
        String username = "nonexistent";
        String password = "testpass";

        // Mock依赖
        when(userDetailsService.loadUserByUsername(username)).thenReturn(null);

        // 创建认证令牌
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(username, password);

        // 执行认证并验证异常
        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
            () -> authenticationProvider.authenticate(authToken));

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("认证提供者 - 支持的认证类型")
    void testAuthenticationProvider_Supports() {
        assertTrue(authenticationProvider.supports(UsernamePasswordAuthenticationToken.class));
        assertFalse(authenticationProvider.supports(String.class));
    }

    @Test
    @DisplayName("认证过滤器 - 基本配置")
    void testAuthenticationFilter_BasicConfig() {
        assertNotNull(authenticationFilter);
        // 验证过滤器配置
        assertEquals("/login/password", authenticationFilter.getFilterProcessesUrl());
    }

    @Test
    @DisplayName("认证令牌 - 未认证状态")
    void testAuthenticationToken_Unauthenticated() {
        String username = "testuser";
        String password = "testpass";

        UsernamePasswordAuthenticationToken token =
            new UsernamePasswordAuthenticationToken(username, password);

        assertEquals(username, token.getPrincipal());
        assertEquals(password, token.getCredentials());
        assertFalse(token.isAuthenticated());
        assertNull(token.getAuthorities());
    }

    @Test
    @DisplayName("认证令牌 - 已认证状态")
    void testAuthenticationToken_Authenticated() {
        String username = "testuser";
        var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        UsernamePasswordAuthenticationToken token =
            new UsernamePasswordAuthenticationToken(username, authorities);

        assertEquals(username, token.getPrincipal());
        assertNull(token.getCredentials());
        assertTrue(token.isAuthenticated());
        assertEquals(1, token.getAuthorities().size());
    }
}