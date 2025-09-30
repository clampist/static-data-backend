package com.staticdata.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Token refresh request DTO
 */
@Data
@Accessors(chain = true)
@Schema(description = "Token refresh request")
public class RefreshTokenRequest {

    @Schema(description = "Refresh token")
    private String refreshToken;
}