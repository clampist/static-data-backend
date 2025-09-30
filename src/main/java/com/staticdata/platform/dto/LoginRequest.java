package com.staticdata.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Login Request DTO
 */
@Data
@Accessors(chain = true)
@Schema(description = "User login request")
public class LoginRequest {

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 50, message = "Username length must be between 3-50 characters")
    @Schema(description = "Username", example = "admin")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 100, message = "Password length must be between 6-100 characters")
    @Schema(description = "Password", example = "password123")
    private String password;
}