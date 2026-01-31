package com.universe.life.user.privacy.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户简单VO（用于部门员工列表）
 *
 * @author 毛伟然
 * @since 2026-01-17
 */
@Data
@Schema(description = "系统用户简单VO")
public class SysUserSimpleVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;
    
    @Schema(description = "是否主部门")
    private Boolean isPrimary;
    
    @Schema(description = "加入时间")
    private LocalDateTime joinedAt;
}
