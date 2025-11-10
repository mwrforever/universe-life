package com.universe.life.user.privacy.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户详情表
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_detail")
public class UserDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private Long id;

    /**
     * 扩展字段（JSON）
     */
    private String ext;

    /**
     * 简介
     */
    private String bio;

    /**
     * 是否允许接收订单：0 否 1 是
     */
    private Integer receiveOrder;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 国家
     */
    private String country;

    /**
     * 街道地址
     */
    private String road;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 邮编
     */
    private String postcode;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;


}
