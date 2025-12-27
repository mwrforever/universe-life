package com.universe.life.auth.resource.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.universe.life.api.client.UserClient;
import com.universe.life.auth.common.constants.RedisConstants;
import com.universe.life.auth.common.domain.dto.AdminAuthInfo;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.model.domain.dto.UserInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.concurrent.TimeUnit;

/**
 * @author 毛伟然
 * @since 2025/12/21 14:38
 */
@RequiredArgsConstructor
public class AdminAuthInfoService implements UserDetailsService {

    private final UserClient userClient;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 获取用户信息
        UserInfoDTO userInfo = userClient.login(username);
        if (ObjectUtil.isNull(userInfo)) {
            throw new UsernameNotFoundException(ExceptionMessage.COMMON_ERROR);
        }
        // TODO 获取用户权限

        // 封装用户信息
        AdminAuthInfo info = new AdminAuthInfo(userInfo.getId(), userInfo.getUsername(), userInfo.getPassword(), null, userInfo.getAvatar());
        // 将数据缓存到 redis 中
        String key = RedisConstants.USER_AUTH_UID_KEY + userInfo.getId();
        stringRedisTemplate.opsForHash().put(
                key,
                RedisConstants.AUTH_USER_DATA,
                JSONUtil.toJsonStr(info)
        );
        stringRedisTemplate.opsForHash().put(
                key,
                RedisConstants.AUTH_USER_TYPE,
                "admin"
        );
        // 设置过期时间
        stringRedisTemplate.expire(
                key,
                RedisConstants.AUTH_USER_CAPTCHA_EXPIRE_TIME + RandomUtil.randomInt(10, 30),
                TimeUnit.MINUTES
        );
        return info;
    }
}
