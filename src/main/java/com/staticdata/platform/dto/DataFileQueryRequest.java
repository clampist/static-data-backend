package com.staticdata.platform.dto;

import com.staticdata.platform.entity.DataFile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DataFileQueryRequest {
    
    @Schema(description = "文件名关键词", example = "用户")
    private String name;
    
    @Schema(description = "所属模块ID", example = "1")
    private Long organizationNodeId;
    
    @Schema(description = "文件所有者ID", example = "1")
    private Long ownerId;
    
    @Schema(description = "访问级别", example = "PUBLIC")
    private DataFile.AccessLevel accessLevel;
    
    @Schema(description = "数据类型", example = "STRING")
    private DataFile.ColumnDefinition.DataType dataType;
    
    @Schema(description = "页码", example = "1")
    private Integer page = 1;
    
    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;
    
    @Schema(description = "排序字段", example = "createdAt")
    private String sortBy = "createdAt";
    
    @Schema(description = "排序方向", example = "desc", allowableValues = {"asc", "desc"})
    private String sortDirection = "desc";
}
