package com.universe.life.auth.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.universe.life.auth.service.domain.po.Oauth2Jwk;

import java.util.List;

/**
 * <p>
 * JWK表：存储JWK密钥对，用于签名和验证JWT令牌 服务类
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-10
 */
public interface IOauth2JwkService extends IService<Oauth2Jwk> {


    /**
     * 管理员强制更新JWK密钥对
     *
     * @param checkPasswordDTO 管理员密码
     * @return 状态
     */
    Boolean update(String checkPasswordDTO);

    /**
     * 批量保存JWK密钥对
     *
     * @param jwkids JWK密钥对ID列表
     * @return 状态
     */
    Boolean saveBatch(List<String> jwkids);

}
