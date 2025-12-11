package com.universe.life.user.privacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.universe.life.user.privacy.domain.dto.request.UserDetailUpdateRequest;
import com.universe.life.user.privacy.domain.po.UserDetail;
import com.universe.life.user.privacy.domain.vo.UserDetailVO;

/**
 * 用户详情服务接口
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
public interface IUserDetailService extends IService<UserDetail> {

    /**
     * 根据用户ID获取用户详情
     *
     * @param userId 用户ID
     * @return 用户详情VO
     */
    UserDetailVO getUserDetailByUserId(Long userId);

    /**
     * 更新用户详情
     *
     * @param userId  用户ID
     * @param request 更新请求
     * @return 用户详情VO
     */
    UserDetailVO updateUserDetail(Long userId, UserDetailUpdateRequest request);
}
