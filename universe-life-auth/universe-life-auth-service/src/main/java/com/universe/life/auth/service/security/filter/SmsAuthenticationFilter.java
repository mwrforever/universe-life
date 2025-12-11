package com.universe.life.auth.service.security.filter;

import cn.hutool.core.util.StrUtil;
import com.universe.life.auth.service.security.token.SmsAuthenticationToken;
import com.universe.life.common.enums.CaptchaUsageType;
import com.universe.life.auth.common.exception.AuthException;
import com.universe.life.auth.common.message.ExceptionMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

/**
 * SMS验证码认证过滤器
 * 支持表单提交
 *
 * @author 毛伟然
 * @since 2025/11/20 16:22
 */
@Slf4j
public class SmsAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String IDENTIFICATION = "identification";
    private static final String VERIFY_CODE = "verifyCode";
    private static final String USAGE_TYPE = "usageType";

    public SmsAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(new AntPathRequestMatcher("/login/code", "POST"));
        setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        String identification = obtainIdentification(request);
        String verifyCode = obtainVerifyCode(request);
        String usageType = obtainUsageType(request);

        // 校验数据是否存在
        if (StrUtil.isBlank(identification) || StrUtil.isBlank(verifyCode)) {
            throw new AuthException.AuthenticationException(ExceptionMessage.AUTH_FAILED);
        }

        // 去除标识首尾空格
        identification = identification.trim();

        // 如果标识包含@符号，认为是邮箱，转换为小写
        if (identification.contains("@")) {
            identification = identification.toLowerCase();
        }

        // 封装认证Token，默认使用登录用途类型
        SmsAuthenticationToken authenticationToken = new SmsAuthenticationToken(
            identification, verifyCode, CaptchaUsageType.of(Integer.valueOf(usageType)));

        // 设置详细信息
        authenticationToken.setDetails(this.authenticationDetailsSource.buildDetails(request));

        return this.getAuthenticationManager().authenticate(authenticationToken);
    }

    /**
     * 从请求中获取标识
     */
    protected String obtainIdentification(HttpServletRequest request) {
        return request.getParameter(IDENTIFICATION);
    }

    /**
     * 从请求中获取验证码
     */
    protected String obtainVerifyCode(HttpServletRequest request) {
        return request.getParameter(VERIFY_CODE);
    }

    /**
     * 从请求中获取用途类型
     */
    protected String obtainUsageType(HttpServletRequest request) {
        String usageType = request.getParameter(USAGE_TYPE);
        // 默认为登录（1）
        return StrUtil.isBlank(usageType) ? "1" : usageType;
    }
}
