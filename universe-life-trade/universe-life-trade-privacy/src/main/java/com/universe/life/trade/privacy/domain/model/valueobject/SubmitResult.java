package com.universe.life.trade.privacy.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 提交成果值对象
 */
@Getter
@EqualsAndHashCode
public class SubmitResult implements Serializable {

    /**
     * 提交内容描述
     */
    private final String content;

    /**
     * 提交图片列表
     */
    private final List<String> images;

    /**
     * 提交时间
     */
    private final LocalDateTime submittedAt;

    private SubmitResult(String content, List<String> images, LocalDateTime submittedAt) {
        this.content = content;
        this.images = images != null ? Collections.unmodifiableList(images) : Collections.emptyList();
        this.submittedAt = submittedAt;
    }

    /**
     * 创建提交成果
     */
    public static SubmitResult of(String content, List<String> images) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("提交内容不能为空");
        }
        return new SubmitResult(content, images, LocalDateTime.now());
    }

    /**
     * 从持久化数据重建
     */
    public static SubmitResult reconstitute(String content, List<String> images, LocalDateTime submittedAt) {
        if (content == null || content.isBlank()) {
            return null;
        }
        return new SubmitResult(content, images, submittedAt);
    }

    /**
     * 是否有图片
     */
    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }

    /**
     * 获取图片数量
     */
    public int getImageCount() {
        return images != null ? images.size() : 0;
    }

    @Override
    public String toString() {
        return String.format("SubmitResult{content='%s', imageCount=%d, submittedAt=%s}",
                content.length() > 20 ? content.substring(0, 20) + "..." : content,
                getImageCount(),
                submittedAt);
    }
}
