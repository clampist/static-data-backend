package com.staticdata.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Update Organization Node Request DTO
 */
@Data
public class UpdateOrganizationNodeRequest {
    
    @NotBlank(message = "Node name cannot be empty")
    @Size(min = 2, max = 50, message = "Node name length must be between 2-50 characters")
    private String name;
    
    @Size(max = 200, message = "Description length cannot exceed 200 characters")
    private String description;
    
    private Integer sortOrder;
}
