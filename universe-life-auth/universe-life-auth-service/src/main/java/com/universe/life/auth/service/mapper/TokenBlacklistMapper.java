package com.universe.life.auth.service.mapper;

import com.universe.life.auth.service.domain.po.TokenBlacklist;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 令牌黑名单表：存储被撤销的JWT令牌 Mapper 接口
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-10
 */
public interface TokenBlacklistMapper extends BaseMapper<TokenBlacklist> {

}
