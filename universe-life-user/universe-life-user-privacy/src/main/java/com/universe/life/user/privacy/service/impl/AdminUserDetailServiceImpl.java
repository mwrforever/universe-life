package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.user.privacy.domain.dto.request.UserDetailUpdateRequest;
import com.universe.life.user.privacy.domain.po.UserDetail;
import com.universe.life.user.privacy.domain.vo.UserDetailVO;
import com.universe.life.user.privacy.mapper.UserDetailMapper;
import com.universe.life.user.privacy.mapstruct.UserDetailMapstruct;
import com.universe.life.user.privacy.service.IAdminUserDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 管理员用户详情服务实现类
 *
 * @author 毛伟然
 * @since 2025-12-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserDetailServiceImpl extends ServiceImpl<UserDetailMapper, UserDetail> implements IAdminUserDetailService {

    private final UserDetailMapstruct userDetailMapstruct;

    @Override
    public UserDetailVO getUserDetailByUserId(Long id) {
        log.info("管理员获取用户详情，用户ID：{}", id);

        UserDetail detail = this.lambdaQuery()
                .select(
                        UserDetail::getId,
                        UserDetail::getExt,
                        UserDetail::getBio,
                        UserDetail::getReceiveOrder,
                        UserDetail::getBirthday,
                        UserDetail::getProvince,
                        UserDetail::getCity,
                        UserDetail::getCountry,
                        UserDetail::getRoad,
                        UserDetail::getAddress,
                        UserDetail::getCreatedAt,
                        UserDetail::getUpdatedAt
                )
                .eq(UserDetail::getId, id)
                .one();

        // 判空
        if (ObjectUtil.isNull(detail)) {
            // 如果没有详情记录，返回一个空的VO对象
            return new UserDetailVO();
        }

        // 在Service层完成PO到VO的转换
        return userDetailMapstruct.toVO(detail);
    }

    @Override
    public UserDetailVO updateUserDetail(Long id, UserDetailUpdateRequest request) {
        log.info("管理员更新用户详情，用户ID：{}", id);
        // 转换成实体对象
        UserDetail po = userDetailMapstruct.toPO(request);
        po.setId(id);
        // 更新用户详情
        updateById(po);
        // 在Service层完成PO到VO的转换
        return userDetailMapstruct.toVO(po);
    }
}