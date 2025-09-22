package com.staticdata.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 登录响应DTO
 */
@Data
@Accessors(chain = true)
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "JWT访问令牌")
    private String accessToken;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "令牌过期时间（毫秒）")
    private Long expiresIn;

    @Schema(description = "用户信息")
    private UserDto user;

    public LoginResponse(String accessToken, Long expiresIn, UserDto user) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }
}