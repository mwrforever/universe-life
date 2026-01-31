package com.universe.life.common.domain;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页响应结果封装
 *
 * @author universe-life
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer current;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(List<T> records, Long total, Integer current, Integer size) {
        return PageResult.<T>builder()
                .records(records)
                .total(total)
                .current(current)
                .size(size)
                .pages(calculatePages(total, size))
                .build();
    }

    public static <T> PageResult<T> of(List<T> records, IPage<?> page) {
        return of(records, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 计算总页数
     */
    private static Integer calculatePages(Long total, Integer size) {
        if (total == null || size == null || size == 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / size);
    }

    public static <T> PageResult<T> empty(Integer current, Integer size) {
        return of(Collections.emptyList(), 0L, current, size);
    }

    public static <T> PageResult<T> empty(IPage<?> page) {
        return PageResult.empty((int) page.getCurrent(), (int) page.getSize());
    }
}
