package com.staticdata.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新组织节点请求DTO
 */
@Data
public class UpdateOrganizationNodeRequest {
    
    @NotBlank(message = "节点名称不能为空")
    @Size(min = 2, max = 50, message = "节点名称长度必须在2-50个字符之间")
    private String name;
    
    @Size(max = 200, message = "描述长度不能超过200个字符")
    private String description;
    
    private Integer sortOrder;
}
