package com.universe.life.user.privacy.service.impl;

import com.universe.life.user.privacy.domain.po.CertificateToken;
import com.universe.life.user.privacy.mapper.CertificateTokenMapper;
import com.universe.life.user.privacy.service.ICertificateTokenService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户token认证表 服务实现类
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-03
 */
@Service
public class CertificateTokenServiceImpl extends ServiceImpl<CertificateTokenMapper, CertificateToken> implements ICertificateTokenService {

}
