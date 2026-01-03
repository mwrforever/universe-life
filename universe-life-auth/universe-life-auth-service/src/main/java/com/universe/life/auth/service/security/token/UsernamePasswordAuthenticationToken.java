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
 * 用户名密码认证令牌
 *
 * @author 毛伟然
 * @since 2025/11/21
 */
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class UsernamePasswordAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final Object credentials;
    private final Object loginType;

    /**
     * 【新增】Jackson 反序列化专用构造函数
     * 用于从数据库 JSON 还原对象，必须处理父类状态（authorities, details, authenticated）
     */
    @JsonCreator
    public UsernamePasswordAuthenticationToken(
            @JsonProperty("principal") Object principal,
            @JsonProperty("credentials") Object credentials,
            @JsonProperty("authorities") Collection<? extends GrantedAuthority> authorities,
            @JsonProperty("details") Object details,
            @JsonProperty("loginType") Object loginType,
            @JsonProperty("authenticated") boolean authenticated) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        this.loginType = loginType;
        this.setDetails(details); // 还原 details
        super.setAuthenticated(authenticated); // 还原认证状态
    }

    /**
     * 未认证的构造函数
     */
    public UsernamePasswordAuthenticationToken(Object principal, Object credentials, Object loginType) {
        super(null);
        this.loginType = loginType;
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(false);
    }

    /**
     * 已认证的构造函数
     */
    public UsernamePasswordAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = null;
        this.loginType = null;
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