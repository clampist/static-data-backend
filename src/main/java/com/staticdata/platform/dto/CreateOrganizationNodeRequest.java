package com.staticdata.platform.dto;

import com.staticdata.platform.entity.OrganizationNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建组织节点请求DTO
 */
@Data
public class CreateOrganizationNodeRequest {
    
    @NotBlank(message = "节点名称不能为空")
    @Size(min = 2, max = 50, message = "节点名称长度必须在2-50个字符之间")
    private String name;
    
    @Size(max = 200, message = "描述长度不能超过200个字符")
    private String description;
    
    @NotNull(message = "节点类型不能为空")
    private OrganizationNode.NodeType type;
    
    private Long parentId;
    
    private Integer sortOrder = 0;
}
