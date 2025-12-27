package com.universe.life.user.privacy.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 批量操作结果VO
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Data
@Schema(description = "批量操作结果VO")
public class BatchResultVO {

    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failCount;

    public BatchResultVO() {
        this.successCount = 0;
        this.failCount = 0;
    }

    public BatchResultVO(Integer successCount, Integer failCount) {
        this.successCount = successCount;
        this.failCount = failCount;
    }

    public static BatchResultVO success(Integer count) {
        return new BatchResultVO(count, 0);
    }
}
