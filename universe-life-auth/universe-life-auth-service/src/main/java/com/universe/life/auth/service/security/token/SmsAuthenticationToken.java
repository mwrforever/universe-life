package com.universe.life.auth.service.security.token;

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
public class SmsAuthenticationToken extends AbstractAuthenticationToken {


    private final Object principal;
    private final Object credentials;
    private final Object usageType;

    public SmsAuthenticationToken(Object principal, Object credentials, Object usageType) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        this.usageType = usageType;
        setAuthenticated(false);
    }

    public SmsAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = null;
        this.usageType = null;
        super.setAuthenticated(true);
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
