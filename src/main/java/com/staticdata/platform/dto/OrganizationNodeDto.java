package com.staticdata.platform.dto;

import com.staticdata.platform.entity.OrganizationNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 组织节点DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationNodeDto {
    
    private Long id;
    private String name;
    private String description;
    private OrganizationNode.NodeType type;
    private Long parentId;
    private String parentName;
    private Integer sortOrder;
    private List<OrganizationNodeDto> children;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // 统计信息
    private Long childrenCount;
    private Long dataFilesCount;
}
