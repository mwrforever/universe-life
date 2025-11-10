package com.universe.life.auth.service.service;

import com.universe.life.auth.service.domain.po.TokenBlacklist;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 令牌黑名单表：存储被撤销的JWT令牌 服务类
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-10
 */
public interface ITokenBlacklistService extends IService<TokenBlacklist> {

}
