package com.universe.life.auth.gateway.manager;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author 毛伟然
 * @since 2025/11/11 23:19
 */
@RequiredArgsConstructor
public class JwtDecoderManager {

    private final AtomicReference<ReactiveJwtDecoder> decodeCache = new AtomicReference<>();

    private final String issuer;

    public ReactiveJwtDecoder getDecoder() {
        return decodeCache.get();
    }

    public void setDecoder(ReactiveJwtDecoder reactiveJwtDecoder) {
        decodeCache.set(reactiveJwtDecoder);
    }

    public void resetDecoder() {
        ReactiveJwtDecoder reactiveJwtDecoder = ReactiveJwtDecoders.fromOidcIssuerLocation(issuer);
        setDecoder(reactiveJwtDecoder);
    }

}
