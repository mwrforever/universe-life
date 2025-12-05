package com.universe.life.user.privacy.domain.dto.request;

import com.universe.life.model.enums.UserAuthType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author 毛伟然
 * @since 2025/12/4 14:18
 */
@Data
@Schema(description = "删除用户认证方式请求参数")
public class DeleteUserAuthRequest {

    @NotNull(message = "认证方式不能为空")
    @Schema(description = "认证方式")
    private UserAuthType identificationType;

    @Schema(description = "认证信息")
    @NotNull(message = "认证信息不能为空")
    private String identification;

    @Schema(description = "密码")
    @NotNull(message = "密码不能为空")
    private String password;
}
