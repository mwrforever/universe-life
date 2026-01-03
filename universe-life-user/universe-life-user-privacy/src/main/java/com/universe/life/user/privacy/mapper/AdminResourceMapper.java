package com.universe.life.user.privacy.mapper;

import com.universe.life.user.privacy.domain.po.Resource;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 系统资源表 Mapper 接口
 * </p>
 *
 * @author 毛伟然
 * @since 2025-12-01
 */
public interface AdminResourceMapper extends BaseMapper<Resource> {

    /**
     * 根据资源id查询权限
     * @param userId 用户id
     * @return 权限
     */
    List<String> selectPermissions(Long userId);
}
