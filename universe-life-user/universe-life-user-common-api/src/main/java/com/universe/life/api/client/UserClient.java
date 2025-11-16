package com.universe.life.api.client;

import com.universe.life.api.fallback.UserClientFallback;
import com.universe.life.model.domain.dto.RegisterFormDTO;
import com.universe.life.model.domain.dto.UserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author 毛伟然
 * @since 2025/11/1 10:06
 */
@FeignClient(value = "user-service", fallbackFactory = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/users/auth/getUserInfo")
    UserInfoDTO getUserInfo(String username);

    @PostMapping("/users/add")
    void add(RegisterFormDTO registerFormDTO);


}
