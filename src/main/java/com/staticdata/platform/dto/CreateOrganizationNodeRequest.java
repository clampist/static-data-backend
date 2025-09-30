package com.staticdata.platform.dto;

import com.staticdata.platform.entity.OrganizationNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Create Organization Node Request DTO
 */
@Data
public class CreateOrganizationNodeRequest {
    
    @NotBlank(message = "Node name cannot be empty")
    @Size(min = 2, max = 50, message = "Node name length must be between 2-50 characters")
    private String name;
    
    @Size(max = 200, message = "Description length cannot exceed 200 characters")
    private String description;
    
    @NotNull(message = "Node type cannot be empty")
    private OrganizationNode.NodeType type;
    
    private Long parentId;
    
    private Integer sortOrder = 0;
}
