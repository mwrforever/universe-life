package com.universe.life.auth.resource.util;

import com.universe.life.common.domain.dto.UserAuthInfo;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author 毛伟然
 * @since 2025/12/4 13:55
 */
public class SecurityUtil {

    public static Long getUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserAuthInfo userAuthInfo)) {
            return null;
        }
        return userAuthInfo.getId();
    }


    public static String getUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserAuthInfo userAuthInfo)) {
            return null;
        }
        return userAuthInfo.getUsername();
    }
}
