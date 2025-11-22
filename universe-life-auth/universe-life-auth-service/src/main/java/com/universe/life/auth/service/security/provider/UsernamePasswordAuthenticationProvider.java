package com.universe.life.auth.service.security.provider;

import com.universe.life.auth.service.security.token.UsernamePasswordAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 用户名密码认证提供者
 *
 * @author 毛伟然
 * @since 2025/11/21
 */
@Slf4j
@Component
public class UsernamePasswordAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public UsernamePasswordAuthenticationProvider(UserDetailsService userDetailsService,
                                                 PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) authentication;
        String username = String.valueOf(authToken.getPrincipal());
        String password = String.valueOf(authToken.getCredentials());

        log.debug("开始用户名密码认证 - 用户名: {}", username);

        try {
            // 通过用户名加载用户信息
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (userDetails == null) {
                log.warn("用户不存在: {}", username);
                throw new BadCredentialsException("用户名或密码错误");
            }

            // 验证密码
            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                log.warn("密码验证失败: {}", username);
                throw new BadCredentialsException("用户名或密码错误");
            }

            // 检查用户是否被锁定
            if (!userDetails.isAccountNonLocked()) {
                log.warn("用户已被锁定: {}", username);
                throw new BadCredentialsException("用户已被锁定");
            }

            // 检查用户是否启用
            if (!userDetails.isEnabled()) {
                log.warn("用户已被禁用: {}", username);
                throw new BadCredentialsException("用户已被禁用");
            }

            // 检查用户是否过期
            if (!userDetails.isAccountNonExpired()) {
                log.warn("用户已过期: {}", username);
                throw new BadCredentialsException("用户已过期");
            }

            // 检查用户凭证是否过期
            if (!userDetails.isCredentialsNonExpired()) {
                log.warn("用户凭证已过期: {}", username);
                throw new BadCredentialsException("用户凭证已过期");
            }

            log.info("用户认证成功: {}", username);

            // 创建认证成功的Authentication
            UsernamePasswordAuthenticationToken authenticatedToken =
                new UsernamePasswordAuthenticationToken(userDetails, userDetails.getAuthorities());
            authenticatedToken.setDetails(authToken.getDetails());

            return authenticatedToken;

        } catch (BadCredentialsException e) {
            // 重新抛出BadCredentialsException
            throw e;
        } catch (Exception e) {
            log.error("用户名密码认证过程中发生异常", e);
            throw new BadCredentialsException("认证失败");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}