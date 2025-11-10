package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.common.exception.DatabaseException;
import com.universe.life.common.message.ExceptionMessage;
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
 * @since 2025-11-03
 */
@Service
public class UserAuthServiceImpl extends ServiceImpl<UserAuthMapper, UserAuth> implements IUserAuthService {

    @Override
    public UserInfoDTO getUserInfo(String username) {
        // 从数据库中获取用户密码
        UserAuth userAuth = lambdaQuery()
                .select(UserAuth::getUserId, UserAuth::getPassword)
                .eq(UserAuth::getIdentification, username)
                .one();
        // 判断是否存在
        if (ObjectUtil.isNull(userAuth)) {
            throw new DatabaseException.QueryException(ExceptionMessage.DATA_NOT_FOUND);
        }
        // 封装业务实体对象
        UserInfoDTO userInfoDTO = BeanUtil.toBean(userAuth, UserInfoDTO.class);
        userInfoDTO.setUsername(username);
        return userInfoDTO;
    }
}
