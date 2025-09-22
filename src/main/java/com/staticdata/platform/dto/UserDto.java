package com.staticdata.platform.dto;

import com.staticdata.platform.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户信息DTO
 */
@Data
@Builder
@Accessors(chain = true)
@Schema(description = "用户信息")
public class UserDto {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    @Schema(description = "全名", example = "管理员")
    private String fullName;

    @Schema(description = "用户角色", example = "ADMIN")
    private UserRole role;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "创建者")
    private String createdBy;

    @Schema(description = "更新者")
    private String updatedBy;
}