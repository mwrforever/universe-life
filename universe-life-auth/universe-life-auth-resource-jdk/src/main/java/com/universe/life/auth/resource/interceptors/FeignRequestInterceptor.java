package com.universe.life.auth.resource.interceptors;

import com.universe.life.common.constants.JwtConstants;
import com.universe.life.common.domain.dto.UserAuthInfo;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author 毛伟然
 * @since 2025/11/19 13:57
 */
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof UserAuthInfo userAuthInfo)) {

            return;
        }
        requestTemplate.header(JwtConstants.USER_INFO, userAuthInfo.getId().toString());
    }


}
