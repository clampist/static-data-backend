package com.staticdata.platform.dto;

import com.staticdata.platform.entity.DataFile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DataFileQueryRequest {
    
    @Schema(description = "File name keyword", example = "User")
    private String name;
    
    @Schema(description = "Module ID", example = "1")
    private Long organizationNodeId;
    
    @Schema(description = "File owner ID", example = "1")
    private Long ownerId;
    
    @Schema(description = "Access level", example = "PUBLIC")
    private DataFile.AccessLevel accessLevel;
    
    @Schema(description = "Data type", example = "STRING")
    private DataFile.ColumnDefinition.DataType dataType;
    
    @Schema(description = "Page number", example = "1")
    private Integer page = 1;
    
    @Schema(description = "Page size", example = "10")
    private Integer size = 10;
    
    @Schema(description = "Sort field", example = "createdAt")
    private String sortBy = "createdAt";
    
    @Schema(description = "Sort direction", example = "desc", allowableValues = {"asc", "desc"})
    private String sortDirection = "desc";
}
