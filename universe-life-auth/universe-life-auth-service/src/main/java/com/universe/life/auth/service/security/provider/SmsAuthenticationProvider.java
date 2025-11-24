package com.universe.life.auth.service.security.provider;

import com.universe.life.auth.resource.domain.dto.request.VerifyCodeFormRequest;
import com.universe.life.auth.service.security.token.SmsAuthenticationToken;
import com.universe.life.auth.service.service.IAuthCommonService;
import com.universe.life.common.enums.CaptchaUsageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * @author 毛伟然
 * @since 2025/11/20 12:05
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final IAuthCommonService authCommonService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 获取手机号
        SmsAuthenticationToken smsAuthenticationToken = (SmsAuthenticationToken) authentication;
        String email = String.valueOf(smsAuthenticationToken.getPrincipal());
        String captcha = String.valueOf(smsAuthenticationToken.getCredentials());
        CaptchaUsageType usageType = (CaptchaUsageType) smsAuthenticationToken.getUsageType();
        // 校验验证码
        log.debug("短信验证码认证 - 邮箱: {}", email);
        authCommonService.verifyCaptcha(new VerifyCodeFormRequest(email, captcha, usageType));
        // 通过手机号加载用户信息
        UserDetails details = userDetailsService.loadUserByUsername(email);
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
}
