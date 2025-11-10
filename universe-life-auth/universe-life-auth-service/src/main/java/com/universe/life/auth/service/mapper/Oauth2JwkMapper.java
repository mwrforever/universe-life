package com.universe.life.auth.service.mapper;

import com.universe.life.auth.service.domain.po.Oauth2Jwk;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * JWK表：存储JWK密钥对，用于签名和验证JWT令牌 Mapper 接口
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-10
 */
public interface Oauth2JwkMapper extends BaseMapper<Oauth2Jwk> {

}
