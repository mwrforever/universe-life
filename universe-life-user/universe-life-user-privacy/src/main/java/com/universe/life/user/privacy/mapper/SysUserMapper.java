package com.universe.life.user.privacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.universe.life.user.privacy.domain.dao.query.SysUserListQuery;
import com.universe.life.user.privacy.domain.po.SysUser;
import com.universe.life.user.privacy.domain.vo.SysUserListVO;
import org.apache.ibatis.annotations.Param;

/**
 * 平台员工Mapper接口
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 分页查询员工列表
     *
     * @param page  分页参数
     * @param query 查询参数
     * @return 员工列表
     */
    IPage<SysUserListVO> selectSysUserList(IPage<SysUserListVO> page, @Param("query") SysUserListQuery query);

}
