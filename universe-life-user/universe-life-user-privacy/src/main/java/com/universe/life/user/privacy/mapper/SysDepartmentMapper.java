package com.universe.life.user.privacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.universe.life.user.privacy.domain.dao.query.SysDepartmentListQuery;
import com.universe.life.user.privacy.domain.po.SysDepartment;
import com.universe.life.user.privacy.domain.vo.SysDepartmentListVO;
import org.apache.ibatis.annotations.Param;

/**
 * 部门Mapper接口
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
public interface SysDepartmentMapper extends BaseMapper<SysDepartment> {

    /**
     * 分页查询部门列表
     *
     * @param page  分页参数
     * @param query 查询参数
     * @return 部门列表
     */
    IPage<SysDepartmentListVO> selectDepartmentList(IPage<SysDepartmentListVO> page, @Param("query") SysDepartmentListQuery query);
}
