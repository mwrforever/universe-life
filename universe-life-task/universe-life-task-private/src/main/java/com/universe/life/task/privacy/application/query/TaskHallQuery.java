package com.universe.life.task.privacy.application.query;

import lombok.Builder;
import lombok.Data;

/**
 * 任务大厅查询
 */
@Data
@Builder
public class TaskHallQuery {

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 最低悬赏金额（分）
     */
    private Long minReward;

    /**
     * 最高悬赏金额（分）
     */
    private Long maxReward;

    /**
     * 排序字段: reward/deadline/created
     */
    private String sortBy;

    /**
     * 排序方向: asc/desc
     */
    private String sortOrder;

    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 当前用户ID（用于判断是否已申请）
     */
    private Long currentUserId;

    /**
     * 获取页码（默认1）
     */
    public int getPageNumOrDefault() {
        return pageNum != null && pageNum > 0 ? pageNum : 1;
    }

    /**
     * 获取每页数量（默认20，最大100）
     */
    public int getPageSizeOrDefault() {
        if (pageSize == null || pageSize <= 0) {
            return 20;
        }
        return Math.min(pageSize, 100);
    }
}
