package com.universe.life.user.privacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.universe.life.user.privacy.domain.dto.request.UserDetailUpdateRequest;
import com.universe.life.user.privacy.domain.po.UserDetail;
import com.universe.life.user.privacy.domain.vo.UserDetailVO;

/**
 * 管理员用户详情服务接口
 *
 * @author 毛伟然
 * @since 2025-12-04
 */
public interface IAdminUserDetailService extends IService<UserDetail> {

    /**
     * 根据用户ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情VO
     */
    UserDetailVO getUserDetailByUserId(Long id);

    /**
     * 更新用户详情
     *
     * @param id 用户ID
     * @param request 更新请求
     */
    void updateUserDetail(Long id, UserDetailUpdateRequest request);
}