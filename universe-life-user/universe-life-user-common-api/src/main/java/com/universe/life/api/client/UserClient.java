package com.universe.life.api.client;

import com.universe.life.api.fallback.UserClientFallback;
import com.universe.life.model.domain.dto.AdminUserInfoDTO;
import com.universe.life.model.domain.dto.RegisterFormDTO;
import com.universe.life.model.domain.dto.UserInfoDTO;
import com.universe.life.model.domain.dto.UserStatusDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 毛伟然
 * @since 2025/11/1 10:06
 */
@FeignClient(value = "user-service", fallbackFactory = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/auth/getUserInfo")
    UserInfoDTO getUserInfo(@RequestParam String username);

    @PostMapping("/add")
    void add(@RequestBody RegisterFormDTO registerFormDTO);

    @GetMapping("/privacy/status")
    UserStatusDTO getUserStatus(@RequestParam String username);

    @GetMapping("/admin/sys-user/login")
    AdminUserInfoDTO login(@RequestParam String username);

}
