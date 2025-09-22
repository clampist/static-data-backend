package com.staticdata.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

/**
 * 数据文件实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Table(name = "data_files")
public class DataFile extends BaseEntity {
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(name = "file_hash", unique = true, nullable = false, length = 32)
    private String fileHash;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_node_id", nullable = false)
    private OrganizationNode organizationNode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessLevel accessLevel = AccessLevel.PRIVATE;
    
    @Column(name = "column_definitions", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<ColumnDefinition> columnDefinitions;
    
    @Column(name = "data_rows", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, Object>> dataRows;
    
    @Column(name = "row_count")
    private Integer rowCount = 0;
    
    @Column(name = "column_count")
    private Integer columnCount = 0;
    
    @OneToMany(mappedBy = "dataFile", cascade = CascadeType.ALL)
    private List<DataFileVersion> versions;
    
    public enum AccessLevel {
        PRIVATE, PUBLIC
    }
    
    @Data
    @Accessors(chain = true)
    public static class ColumnDefinition {
        private String name;
        private DataType dataType;
        private Boolean required = false;
        private String defaultValue;
        private Integer maxLength;
        
        public enum DataType {
            STRING, INTEGER, DECIMAL, BOOLEAN, DATE, DATETIME, JSON
        }
    }
}