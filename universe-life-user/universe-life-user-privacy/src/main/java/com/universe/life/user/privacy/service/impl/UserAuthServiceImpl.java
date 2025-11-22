package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.model.domain.dto.UserInfoDTO;
import com.universe.life.user.privacy.domain.po.UserAuth;
import com.universe.life.user.privacy.mapper.UserAuthMapper;
import com.universe.life.user.privacy.service.IUserAuthService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户认证表 服务实现类
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
@Service
public class UserAuthServiceImpl extends ServiceImpl<UserAuthMapper, UserAuth> implements IUserAuthService {

    @Override
    public UserInfoDTO getUserInfo(String username) {
        UserAuth userAuth = lambdaQuery()
                .select(
                        UserAuth::getUserId,
                        UserAuth::getPassword
                )
                .eq(UserAuth::getIdentification, username)
                .one();
        // 判断账号是否存在
        if (ObjectUtil.isNull(userAuth)) {
            return null;
        }
        return BeanUtil.toBean(userAuth, UserInfoDTO.class);
    }
}
