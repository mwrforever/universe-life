package com.universe.life.user.privacy;

import com.universe.life.model.domain.dto.RegisterFormDTO;
import com.universe.life.model.enums.UserAuthType;
import com.universe.life.user.privacy.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author 毛伟然
 * @since 2025/11/17 10:09
 */
//@SpringBootTest
public class UserLoginTest {

    @Autowired
    private IUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testAddAdminUser() {
        userService.add(new RegisterFormDTO("admin", passwordEncoder.encode("admin"), UserAuthType.EMAIL, "18229923842@163.com"));
    }

}
