package com.staticdata.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Login Response DTO
 */
@Data
@Accessors(chain = true)
@Schema(description = "Login response")
public class LoginResponse {

    @Schema(description = "JWT access token")
    private String accessToken;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Token expiration time (milliseconds)")
    private Long expiresIn;

    @Schema(description = "User information")
    private UserDto user;

    public LoginResponse(String accessToken, Long expiresIn, UserDto user) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }
}