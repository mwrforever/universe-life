package com.universe.life.model.domain.dto;

import com.universe.life.model.enums.UserAuthType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 毛伟然
 * @since 2025/11/4 11:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户登录基础表单")
public class LoginFormBaseDTO {

    @Schema(description = "用户名")

    private String username;

    private Boolean remember;

    private UserAuthType type;

}
