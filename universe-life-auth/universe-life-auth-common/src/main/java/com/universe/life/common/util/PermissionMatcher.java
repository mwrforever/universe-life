package com.universe.life.common.util;

import com.universe.life.common.domain.dto.UserAuthInfo;
import com.universe.life.common.message.ExceptionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author 毛伟然
 * @since 2025/11/25 14:54
 */
@RequiredArgsConstructor
public class PermissionMatcher {


    private final AntRequestMatchUtil antRequestMatchUtil;

    public boolean match(String permission) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserAuthInfo userAuthInfo)) {
            throw new ClassCastException(ExceptionMessage.COMMON_ERROR);
        }
        return antRequestMatchUtil.matchAnyPermission(userAuthInfo.getPrePermissions(), permission);
    }

}
