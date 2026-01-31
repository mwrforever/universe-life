package com.universe.life.task.privacy.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 发布者信息VO
 */
@Data
@Schema(description = "发布者信息")
public class PublisherInfoVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "信用评分")
    private Integer creditScore;

    @Schema(description = "历史发布任务数")
    private Integer publishedTaskCount;

    @Schema(description = "已完成任务数")
    private Integer completedTaskCount;
}
