package com.universe.life.user.privacy.util;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.universe.life.user.privacy.domain.po.SysUser;
import com.universe.life.user.privacy.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 员工工号生成器
 * 工号格式：UL + 部门编码 + 6位递增数字
 * 示例：ULTECH000001, ULHR000001
 *
 * @author 毛伟然
 * @since 2025/12/12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeNoGenerator {

    private static final String PREFIX = "UL";
    private static final int SEQUENCE_LENGTH = 6;

    private final SysUserMapper sysUserMapper;

    /**
     * 生成员工工号
     *
     * @param deptCode 部门编码
     * @return 工号
     */
    public String generate(String deptCode) {
        String prefix = PREFIX + (deptCode != null ? deptCode.toUpperCase() : "");
        
        // 查询该部门前缀下的最大工号
        String maxEmployeeNo = getMaxEmployeeNoByPrefix(prefix);
        
        // 计算下一个序号
        int nextSeq = 1;
        if (maxEmployeeNo != null && maxEmployeeNo.length() > prefix.length()) {
            try {
                String seqStr = maxEmployeeNo.substring(prefix.length());
                nextSeq = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                log.warn("解析工号序号失败，使用随机序号: {}", maxEmployeeNo);
                nextSeq = RandomUtil.randomInt(1, 999999);
            }
        }
        
        // 格式化序号为6位
        String seqStr = String.format("%0" + SEQUENCE_LENGTH + "d", nextSeq);
        String employeeNo = prefix + seqStr;
        
        log.debug("生成工号: {}, 部门编码: {}", employeeNo, deptCode);
        return employeeNo;
    }

    /**
     * 生成管理员工号（无部门）
     *
     * @return 工号
     */
    public String generateAdmin() {
        return generate("ADMIN");
    }

    /**
     * 查询指定前缀下的最大工号
     *
     * @param prefix 工号前缀
     * @return 最大工号
     */
    private String getMaxEmployeeNoByPrefix(String prefix) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(SysUser::getEmployeeNo, prefix)
                .orderByDesc(SysUser::getEmployeeNo)
                .last("LIMIT 1");
        
        SysUser sysUser = sysUserMapper.selectOne(wrapper);
        return sysUser != null ? sysUser.getEmployeeNo() : null;
    }

}
