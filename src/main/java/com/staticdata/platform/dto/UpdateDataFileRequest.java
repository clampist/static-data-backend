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
    
    @Size(min = 2, max = 100, message = "文件名长度必须在2到100个字符之间")
    @Schema(description = "数据文件名称", example = "用户基础数据表")
    private String name;
    
    @Size(max = 500, message = "描述长度不能超过500个字符")
    @Schema(description = "数据文件描述", example = "存储用户基础信息，包括姓名、邮箱、手机号等")
    private String description;
    
    @Schema(description = "访问级别", example = "PUBLIC", allowableValues = {"PRIVATE", "PUBLIC"})
    private DataFile.AccessLevel accessLevel;
    
    @Valid
    @Schema(description = "列定义列表")
    private List<ColumnDefinitionRequest> columnDefinitions;
    
    @Schema(description = "数据行列表")
    private List<Map<String, Object>> dataRows;
    
    @Data
    public static class ColumnDefinitionRequest {
        @Size(max = 50, message = "列名长度不能超过50个字符")
        @Schema(description = "列名", example = "username")
        private String name;
        
        @Schema(description = "数据类型", example = "STRING")
        private DataFile.ColumnDefinition.DataType dataType;
        
        @Schema(description = "是否必填", example = "true")
        private Boolean required;
        
        @Schema(description = "默认值", example = "")
        private String defaultValue;
        
        @Schema(description = "最大长度", example = "50")
        private Integer maxLength;
        
        @Size(max = 200, message = "列描述长度不能超过200个字符")
        @Schema(description = "列描述", example = "用户登录名")
        private String description;
        
        @Size(max = 100, message = "验证规则长度不能超过100个字符")
        @Schema(description = "验证规则", example = "^[a-zA-Z0-9_]+$")
        private String validationRule;
        
        @Schema(description = "列排序", example = "1")
        private Integer sortOrder;
    }
}
