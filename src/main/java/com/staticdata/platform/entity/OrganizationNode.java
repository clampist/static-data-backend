package com.staticdata.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Organization Node Entity (tree structure of Department-Team-Business Direction-Module)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Table(name = "organization_nodes")
public class OrganizationNode extends BaseEntity {
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NodeType type;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @OneToMany(mappedBy = "organizationNode", cascade = CascadeType.ALL)
    private List<DataFile> dataFiles;
    
    public enum NodeType {
        DEPARTMENT, TEAM, BUSINESS_DIRECTION, MODULE
    }
}