package com.universe.life.user.privacy.domain.vo;

import com.universe.life.user.privacy.enums.CommonStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门列表VO
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Data
@Schema(description = "部门列表VO")
public class SysDepartmentListVO {

    @Schema(description = "部门ID")
    private Long id;

    @Schema(description = "父部门ID")
    private Long parentId;

    @Schema(description = "部门编码")
    private String deptCode;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "部门负责人姓名")
    private String leaderName;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "状态")
    private CommonStatus status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
