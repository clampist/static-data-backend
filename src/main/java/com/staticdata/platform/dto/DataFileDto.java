package com.staticdata.platform.dto;

import com.staticdata.platform.entity.DataFile;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DataFileDto {
    private Long id;
    private String name;
    private String description;
    private String fileHash;
    private Long organizationNodeId;
    private String organizationNodeName;
    private String organizationNodePath; // 完整的组织路径，如：总公司/产品部/前端团队/用户体验
    private Long ownerId;
    private String ownerName;
    private DataFile.AccessLevel accessLevel;
    private List<ColumnDefinitionDto> columnDefinitions;
    private List<Map<String, Object>> dataRows;
    private Integer rowCount;
    private Integer columnCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Integer versionCount; // 版本数量
    private String lastModifiedBy; // 最后修改者
    private LocalDateTime lastModifiedAt; // 最后修改时间
    
    @Data
    @Builder
    public static class ColumnDefinitionDto {
        private String name;
        private DataFile.ColumnDefinition.DataType dataType;
        private Boolean required;
        private String defaultValue;
        private Integer maxLength;
        private String description; // 列描述
        private String validationRule; // 验证规则
        private Integer sortOrder; // 列排序
    }
}
