package com.staticdata.platform.dto;

import com.staticdata.platform.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * User Information DTO
 */
@Data
@Builder
@Accessors(chain = true)
@Schema(description = "User information")
public class UserDto {

    @Schema(description = "User ID")
    private Long id;

    @Schema(description = "Username", example = "admin")
    private String username;

    @Schema(description = "Email", example = "admin@example.com")
    private String email;

    @Schema(description = "Full name", example = "Administrator")
    private String fullName;

    @Schema(description = "User role", example = "ADMIN")
    private UserRole role;

    @Schema(description = "Whether enabled", example = "true")
    private Boolean enabled;

    @Schema(description = "Created time")
    private LocalDateTime createdAt;

    @Schema(description = "Updated time")
    private LocalDateTime updatedAt;

    @Schema(description = "Created by")
    private String createdBy;

    @Schema(description = "Updated by")
    private String updatedBy;
}
