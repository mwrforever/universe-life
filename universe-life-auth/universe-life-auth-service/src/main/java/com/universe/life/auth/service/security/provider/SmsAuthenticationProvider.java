package com.universe.life.auth.service.security.provider;

import com.universe.life.auth.common.constants.JwtConstants;
import com.universe.life.auth.common.exception.AuthException;
import com.universe.life.auth.resource.util.VerifyCaptchaUtil;
import com.universe.life.auth.service.security.token.SmsAuthenticationToken;
import com.universe.life.auth.service.util.AuthUtil;
import com.universe.life.common.server.model.domain.domain.enums.CaptchaUsageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Map;

/**
 * @author 毛伟然
 * @since 2025/11/20 12:05
 */
@Slf4j
@RequiredArgsConstructor
public class SmsAuthenticationProvider implements AuthenticationProvider {

    private final Map<String, UserDetailsService> userDetailsServices;
    private final VerifyCaptchaUtil verifyCaptchaUtil;
    private final AuthUtil authUtil;


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 获取手机号
        SmsAuthenticationToken smsAuthenticationToken = (SmsAuthenticationToken) authentication;
        String email = String.valueOf(smsAuthenticationToken.getPrincipal());
        String captcha = String.valueOf(smsAuthenticationToken.getCredentials());
        CaptchaUsageType usageType = (CaptchaUsageType) smsAuthenticationToken.getUsageType();
        String loginType = String.valueOf(smsAuthenticationToken.getLoginType());
        String loginIp = smsAuthenticationToken.getLoginIp() != null ? String.valueOf(smsAuthenticationToken.getLoginIp()) : null;

        // 校验验证码
        log.debug("短信验证码认证 - 邮箱: {}", email);
        boolean verified = verifyCaptchaUtil.verifyCaptcha(usageType, email, captcha);
        if (!verified) {
            log.debug("短信验证码认证 - 邮箱: {}, 验证码错误", email);
            throw new AuthException.AuthenticationException("验证码错误");
        }
        // 通过手机号加载用户信息
        UserDetailsService userDetailsService = getUserDetailsService(loginType);
        if (userDetailsService == null) {
            log.error("用户信息加载服务不存在: {}", loginType);
            throw new BadCredentialsException("用户信息加载服务不存在");
        }
        UserDetails details = userDetailsService.loadUserByUsername(email);

        // 通过 mq异步保存用户登录信息
        authUtil.sendLoginInfoAsync(details, loginType, loginIp);

        // 创建认证成功的authentication
        SmsAuthenticationToken smsAuthenticationTokenResult = new SmsAuthenticationToken(details, details.getAuthorities());
        smsAuthenticationTokenResult.setDetails(details);
        log.debug("短信验证码认证 - 邮箱: {}, 认证成功", email);
        return smsAuthenticationTokenResult;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private UserDetailsService getUserDetailsService(String loginType) {
        String key = JwtConstants.USER_AUTH_INFO_SERVICE;
        if (JwtConstants.EMPLOYEE_LOGIN.equals(loginType)) {
            key = JwtConstants.ADMIN_AUTH_INFO_SERVICE;
        }
        return userDetailsServices.getOrDefault(key, null);
    }

}
