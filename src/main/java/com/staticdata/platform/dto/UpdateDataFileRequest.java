package com.staticdata.platform.dto;

import com.staticdata.platform.entity.DataFile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UpdateDataFileRequest {
    
    @Size(min = 2, max = 100, message = "File name length must be between 2 and 100 characters")
    @Schema(description = "Data File name", example = "User basic data table")
    private String name;
    
    @Size(max = 500, message = "Description length cannot exceed 500 characters")
    @Schema(description = "Data File description", example = "Store user basic information including name, email, phone number, etc.")
    private String description;
    
    @Schema(description = "Access level", example = "PUBLIC", allowableValues = {"PRIVATE", "PUBLIC"})
    private DataFile.AccessLevel accessLevel;
    
    @Valid
    @Schema(description = "Column definition list")
    private List<ColumnDefinitionRequest> columnDefinitions;
    
    @Schema(description = "Data row list")
    private List<Map<String, Object>> dataRows;
    
    @Data
    public static class ColumnDefinitionRequest {
        @Size(max = 50, message = "Column name length cannot exceed 50 characters")
        @Schema(description = "Column name", example = "username")
        private String name;
        
        @Schema(description = "Data type", example = "STRING")
        private DataFile.ColumnDefinition.DataType dataType;
        
        @Schema(description = "Whether required", example = "true")
        private Boolean required;
        
        @Schema(description = "Default value", example = "")
        private String defaultValue;
        
        @Schema(description = "Maximum length", example = "50")
        private Integer maxLength;
        
        @Size(max = 200, message = "Column description length cannot exceed 200 characters")
        @Schema(description = "Column description", example = "User login name")
        private String description;
        
        @Size(max = 100, message = "Validation rule length cannot exceed 100 characters")
        @Schema(description = "Validation rule", example = "^[a-zA-Z0-9_]+$")
        private String validationRule;
        
        @Schema(description = "Column sort order", example = "1")
        private Integer sortOrder;
    }
}
