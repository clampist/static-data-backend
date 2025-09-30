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
    private String organizationNodePath; // Complete organization path, e.g.: Headquarters/Product Department/Frontend Team/User Experience
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
    private Integer versionCount; // Version count
    private String lastModifiedBy; // Last modified by
    private LocalDateTime lastModifiedAt; // Last modified time
    
    @Data
    @Builder
    public static class ColumnDefinitionDto {
        private String name;
        private DataFile.ColumnDefinition.DataType dataType;
        private Boolean required;
        private String defaultValue;
        private Integer maxLength;
        private String description; // Column description
        private String validationRule; // Validation rule
        private Integer sortOrder; // Column sort order
    }
}
