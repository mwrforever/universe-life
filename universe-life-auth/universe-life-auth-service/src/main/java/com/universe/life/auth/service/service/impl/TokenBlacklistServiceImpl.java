package com.universe.life.auth.service.service.impl;

import com.universe.life.auth.service.domain.po.TokenBlacklist;
import com.universe.life.auth.service.mapper.TokenBlacklistMapper;
import com.universe.life.auth.service.service.ITokenBlacklistService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 令牌黑名单表：存储被撤销的JWT令牌 服务实现类
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-10
 */
@Service
public class TokenBlacklistServiceImpl extends ServiceImpl<TokenBlacklistMapper, TokenBlacklist> implements ITokenBlacklistService {

}
