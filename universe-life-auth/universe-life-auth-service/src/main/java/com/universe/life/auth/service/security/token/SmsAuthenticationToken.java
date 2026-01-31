package com.universe.life.auth.service.security.token;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import javax.security.auth.Subject;
import java.util.Collection;

/**
 * @author 毛伟然
 * @since 2025/11/20 12:03
 */
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class SmsAuthenticationToken extends AbstractAuthenticationToken {


    private final Object principal;
    private final Object credentials;
    private final Object usageType;
    private final Object loginType;
    private final Object loginIp;


    @JsonCreator
    public SmsAuthenticationToken(
            @JsonProperty("principal") Object principal,
            @JsonProperty("credentials") Object credentials,
            @JsonProperty("usageType") Object usageType,
            @JsonProperty("authorities") Collection<? extends GrantedAuthority> authorities,
            @JsonProperty("details") Object details,
            @JsonProperty("loginType") Object loginType,
            @JsonProperty("loginIp") Object loginIp,
            @JsonProperty("authenticated") boolean authenticated) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        this.usageType = usageType;
        this.loginType = loginType;
        this.loginIp = loginIp;
        this.setDetails(details); // 还原 details 信息
        super.setAuthenticated(authenticated); // 还原认证状态
    }

    /**
     * 短信验证码登录构造函数
     */
    public SmsAuthenticationToken(Object principal, Object credentials, Object usageType, Object loginType, Object loginIp) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        this.usageType = usageType;
        this.loginType = loginType;
        this.loginIp = loginIp;
        setAuthenticated(false);
    }

    public SmsAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = null;
        this.usageType = null;
        this.loginType = null;
        this.loginIp = null;
        super.setAuthenticated(true);
    }

    @Override
    @JsonIgnore
    public String getName() {
        return super.getName();
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public boolean implies(Subject subject) {
        return super.implies(subject);
    }
}
