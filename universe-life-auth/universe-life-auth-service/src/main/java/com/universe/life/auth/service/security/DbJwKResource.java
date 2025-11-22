package com.universe.life.auth.service.security;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.universe.life.auth.service.constants.RedisConstants;
import com.universe.life.auth.service.domain.po.Oauth2Jwk;
import com.universe.life.auth.service.enums.JwkState;
import com.universe.life.auth.service.manager.JwkManager;
import com.universe.life.auth.service.service.IOauth2JwkService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 毛伟然
 * @since 2025/11/10 09:23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DbJwKResource implements JWKSource<SecurityContext> {

    private final IOauth2JwkService oauth2JwkService;
    private final JwkManager jwkManager;
    private final RedissonClient redissonClient;
    private RLock lock;

    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext securityContext) throws KeySourceException {
        JWKSet jwkSet = isJwkSetEndpointRequest() ? jwkManager.jwkSet() : jwkManager.primaryJwkSet();
        if (ObjectUtil.isNull(jwkSet) || CollUtil.isEmpty(jwkSet.getKeys())) {
            jwkSet = loadJwkSet();
        }
        return jwkSelector.select(jwkSet);
    }


    private JWKSet loadJwkSet() {
        JWKSet jwkSet;
        try {
            if (lock == null) {
                lock = redissonClient.getLock(RedisConstants.AUTH_SECRET_KEY_GENERATE_LOCK);
            }
            lock.lock();
            // 再从数据库中获取密钥对
            List<Oauth2Jwk> oauth2Jwks = oauth2JwkService.lambdaQuery()
                    .select(
                            Oauth2Jwk::getKid,
                            Oauth2Jwk::getPublicKey,
                            Oauth2Jwk::getPrivateKey,
                            Oauth2Jwk::getAlgorithm
                    )
                    .eq(!isJwkSetEndpointRequest(), Oauth2Jwk::getState, JwkState.ACTIVE.getState())
                    .in(isJwkSetEndpointRequest(),Oauth2Jwk::getState, List.of(JwkState.ACTIVE.getState(), JwkState.RESOLVED.getState()))
                    .gt(Oauth2Jwk::getExpireTime, LocalDateTime.now())
                    .list();
            // 如果为空，重新生成密钥对
            jwkSet = jwkManager.loadFromDatabase(oauth2Jwks);
            List<String> primaryKids = new ArrayList<>();
            if (ObjectUtil.isNull(jwkSet)) {
                // 重新生成密钥对，并保存到数据库中
                jwkManager.rotate();
                // 获取密钥对
                jwkSet = jwkManager.jwkSet();
                // 获取密钥对
                primaryKids = jwkManager.allPrimaryKids();
            }
            // 更新到数据库中
            oauth2JwkService.saveBatch(primaryKids);
        } finally {
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return jwkSet;
    }


    /**
     * 判断是否为JWK Set端点请求
     */
    private boolean isJwkSetEndpointRequest() {
        try {
            // 获取当前请求上下文
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes) {
                HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                String uri = request.getRequestURI();
                // 判断是否访问JWK Set端点
                return uri.endsWith("/.well-known/jwks.json") ||
                        uri.contains("/jwks") ||
                        "application/json".equals(request.getHeader("Accept"));
            }
        } catch (Exception e) {
            log.debug("无法获取请求上下文，默认使用签名模式");
        }
        return false;
    }


}
