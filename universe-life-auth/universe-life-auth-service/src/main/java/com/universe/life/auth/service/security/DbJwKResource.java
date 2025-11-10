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
import com.universe.life.auth.service.manager.JwkManager;
import com.universe.life.auth.service.service.IOauth2JwkService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 毛伟然
 * @since 2025/11/10 09:23
 */
@Component
@RequiredArgsConstructor
public class DbJwKResource implements JWKSource<SecurityContext> {

    private final IOauth2JwkService oauth2JwkService;
    private final JwkManager jwkManager;
    private final RedissonClient redissonClient;
    private final RLock lock = redissonClient.getLock(RedisConstants.AUTH_SECRET_KEY_GENERATE_LOCK);

    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext securityContext) throws KeySourceException {
        // 先从JwkManager中获取密钥对
        JWKSet jwkSet = jwkManager.jwkSet();
        if (ObjectUtil.isNotNull(jwkSet) && CollUtil.isNotEmpty(jwkSet.getKeys())) {
            return jwkSelector.select(jwkSet);
        }
        JWKSet newJwkSet;
        try {
            lock.lock();
            // 再从数据库中获取密钥对
            List<Oauth2Jwk> oauth2Jwks = oauth2JwkService.lambdaQuery()
                    .select(
                            Oauth2Jwk::getKid,
                            Oauth2Jwk::getPublicKey,
                            Oauth2Jwk::getPrivateKey,
                            Oauth2Jwk::getAlgorithm
                    )
                    .eq(Oauth2Jwk::getState, "ACTIVE")
                    .gt(Oauth2Jwk::getExpireTime, LocalDateTime.now())
                    .list();
            // 如果为空，重新生成密钥对
            newJwkSet = jwkManager.loadFromDatabase(oauth2Jwks);
            if (ObjectUtil.isNull(newJwkSet)) {
                // 重新生成密钥对，并保存到数据库中
                jwkManager.rotate();
                // 获取密钥对
                newJwkSet = jwkManager.jwkSet();
            }
            // 更新到数据库中
            oauth2JwkService.saveBatch(newJwkSet);
        } finally {
            lock.unlock();
        }
        return jwkSelector.select(newJwkSet);
    }


}
