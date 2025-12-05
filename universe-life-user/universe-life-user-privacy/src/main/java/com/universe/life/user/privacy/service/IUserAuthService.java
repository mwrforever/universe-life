package com.universe.life.user.privacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.universe.life.model.domain.dto.UserInfoDTO;
import com.universe.life.user.privacy.domain.dto.request.DeleteUserAuthRequest;
import com.universe.life.user.privacy.domain.dto.request.UserAuthCreateRequest;
import com.universe.life.user.privacy.domain.dto.request.UserAuthUpdateRequest;
import com.universe.life.user.privacy.domain.po.UserAuth;
import com.universe.life.user.privacy.domain.vo.UserAuthCreateVO;
import com.universe.life.user.privacy.domain.vo.UserAuthListVO;

import java.util.List;

/**
 * 用户认证服务接口
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
public interface IUserAuthService extends IService<UserAuth> {

    /**
     * 获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserInfoDTO getUserInfo(String username);

    /**
     * 创建用户认证
     *
     * @param request 认证创建请求
     * @return 用户认证VO
     */
    UserAuthCreateVO createUserAuth(UserAuthCreateRequest request);

    /**
     * 根据用户ID获取认证方式列表
     *
     * @param userId 用户ID
     * @return 认证方式列表VO
     */
    List<UserAuthListVO> getUserAuthListByUserId(Long userId);

    /**
     * 更新用户认证信息
     *
     * @param request 更新请求
     */
    void updateUserAuth(UserAuthUpdateRequest request);

    /**
     * 删除用户认证
     *
     * @param id 认证ID
     */
    void deleteUserAuth(Long id, DeleteUserAuthRequest request);

    /**
     * 根据用户ID删除认证信息
     *
     * @param userId 用户ID
     */
    void deleteByUserId(Long userId);

    /**
     * 根据用户ID列表批量删除认证信息
     *
     * @param userIds 用户ID列表
     */
    void batchDeleteByUserIds(List<Long> userIds);
}
