package com.universe.life.user.privacy.domain.vo;

import com.universe.life.model.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户状态视图对象
 *
 * @author Claude
 * @since 2025-12-02
 */
@Data
@Schema(description = "用户状态视图对象")
public class UserStatusVO {

    @Schema(description = "用户状态：0-NOMAL(正常) 1-CAN_RECEIVE(可接单) 2-CAN_PUBLISH(可发单) 3-DISABLE(禁用)", example = "NORMAL")
    private UserStatus status;
}