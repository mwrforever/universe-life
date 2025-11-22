package com.universe.life.auth.service.security.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.universe.life.auth.service.security.token.SmsAuthenticationToken;
import com.universe.life.common.enums.CaptchaUsageType;
import com.universe.life.common.exception.AuthException;
import com.universe.life.common.message.ExceptionMessage;
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
 * 支持JSON格式的短信验证码登录
 *
 * @author 毛伟然
 * @since 2025/11/20 16:22
 */
@Slf4j
public class SmsAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String method = "POST";
    private static final String IDENTIFICATION = "identification";
    private static final String VERIFY_CODE = "verifyCode";
    private static final String USAGE_TYPE = "usageType";

    public SmsAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(new AntPathRequestMatcher("/login/code", "POST"));
        setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        // 验证请求方式
        if (!request.getMethod().equals(method)) {
            throw new AuthException.AuthenticationException(String.format("%s: %S", ExceptionMessage.REQUEST_METHOD_NOT_ALLOWED, request.getMethod()));
        }
        // 从请求体中获取JSON数据
        String requestBody = request.getReader().lines().reduce("", String::concat);
        if (StrUtil.isBlank(requestBody)) {
            throw new AuthException.AuthenticationException(ExceptionMessage.AUTH_FAILED);
        }

        // 解析JSON
        JSONObject jsonObject = JSONUtil.parseObj(requestBody);
        String identification = jsonObject.getStr(IDENTIFICATION);
        String verifyCode = jsonObject.getStr(VERIFY_CODE);
        String usageType = jsonObject.getStr(USAGE_TYPE);

        // 校验数据是否存在
        if (StrUtil.isBlank(identification) || StrUtil.isBlank(verifyCode) || StrUtil.isBlank(usageType)) {
            throw new AuthException.AuthenticationException(ExceptionMessage.AUTH_FAILED);
        }

        log.debug("短信验证码认证 - 标识: {}", identification);

        // 封装返回accessToken
        SmsAuthenticationToken smsAuthenticationToken = new SmsAuthenticationToken(identification, verifyCode, CaptchaUsageType.of(Integer.valueOf(usageType)));
        return this.getAuthenticationManager().authenticate(smsAuthenticationToken);
    }
}
