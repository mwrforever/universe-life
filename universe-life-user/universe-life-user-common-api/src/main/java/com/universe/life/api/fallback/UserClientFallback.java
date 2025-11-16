package com.universe.life.api.fallback;

import com.universe.life.api.client.UserClient;
import com.universe.life.model.domain.dto.RegisterFormDTO;
import com.universe.life.model.domain.dto.UserInfoDTO;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * @author 毛伟然
 * @since 2025/11/1 10:08
 */

public class UserClientFallback implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {
            @Override
            public UserInfoDTO getUserInfo(String username) {
                return null;
            }

            @Override
            public void add(RegisterFormDTO registerFormDTO) {
            }
        };
    }
}
