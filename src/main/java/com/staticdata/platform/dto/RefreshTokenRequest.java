package com.staticdata.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 令牌刷新请求DTO
 */
@Data
@Accessors(chain = true)
@Schema(description = "令牌刷新请求")
public class RefreshTokenRequest {

    @Schema(description = "刷新令牌")
    private String refreshToken;
}