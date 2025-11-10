package com.universe.life.user.privacy.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户token认证表
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("certificate_token")
public class CertificateToken implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 认证系列号
     */
    @TableId(value = "series", type = IdType.AUTO)
    private String series;

    /**
     * 认证token
     */
    private String accessToken;

    /**
     * 刷新token
     */
    private String refreshToken;

    /**
     * 有效期
     */
    private Long expiresIn;


}
