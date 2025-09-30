package com.staticdata.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserRegistration Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Registration request")
public class RegisterRequest {

    @Schema(description = "Username", example = "john_doe")
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 50, message = "Username length must be between 3-50 characters")
    private String username;

    @Schema(description = "Email address", example = "john.doe@example.com")
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email format is incorrect")
    @Size(max = 100, message = "Email length cannot exceed 100 characters")
    private String email;

    @Schema(description = "Password", example = "password123")
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 100, message = "Password length must be between 6-100 characters")
    private String password;

    @Schema(description = "Confirm password", example = "password123")
    @NotBlank(message = "Confirm password cannot be empty")
    private String confirmPassword;

    @Schema(description = "Full name", example = "John Doe")
    @NotBlank(message = "Full name cannot be empty")
    @Size(min = 2, max = 100, message = "Full name length must be between 2-100 characters")
    private String fullName;
}