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
 * Data File Version Entity (supports version control, keeps up to 10 versions)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Table(name = "data_file_versions")
public class DataFileVersion extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_file_id", nullable = false)
    private DataFile dataFile;
    
    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;
    
    @Column(name = "change_summary")
    private String changeSummary;
    
    @Column(name = "column_definitions", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<DataFile.ColumnDefinition> columnDefinitions;
    
    @Column(name = "data_rows", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, Object>> dataRows;
    
    @Column(name = "row_count")
    private Integer rowCount = 0;
    
    @Column(name = "column_count")
    private Integer columnCount = 0;
}