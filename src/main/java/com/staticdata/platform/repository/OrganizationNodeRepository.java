package com.staticdata.platform.repository;

import com.staticdata.platform.entity.OrganizationNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 组织节点Repository
 */
@Repository
public interface OrganizationNodeRepository extends JpaRepository<OrganizationNode, Long> {
    
    /**
     * 根据父节点ID查找子节点
     */
    List<OrganizationNode> findByParentIdOrderBySortOrderAsc(Long parentId);
    
    /**
     * 查找根节点（父节点ID为null）
     */
    List<OrganizationNode> findByParentIdIsNullOrderBySortOrderAsc();
    
    /**
     * 根据节点类型查找节点
     */
    List<OrganizationNode> findByTypeOrderBySortOrderAsc(OrganizationNode.NodeType type);
    
    /**
     * 根据名称模糊查询
     */
    @Query("SELECT n FROM OrganizationNode n WHERE n.name LIKE %:name% ORDER BY n.sortOrder ASC")
    List<OrganizationNode> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * 检查同一父节点下是否存在相同名称的节点
     */
    @Query("SELECT COUNT(n) > 0 FROM OrganizationNode n WHERE n.name = :name AND n.parentId = :parentId AND n.id != :excludeId")
    boolean existsByNameAndParentIdAndIdNot(@Param("name") String name, 
                                           @Param("parentId") Long parentId, 
                                           @Param("excludeId") Long excludeId);
    
    /**
     * 检查根节点下是否存在相同名称的节点
     */
    @Query("SELECT COUNT(n) > 0 FROM OrganizationNode n WHERE n.name = :name AND n.parentId IS NULL AND n.id != :excludeId")
    boolean existsByNameAndParentIdIsNullAndIdNot(@Param("name") String name, 
                                                 @Param("excludeId") Long excludeId);
    
    /**
     * 查找所有子节点（递归查询）
     */
    @Query(value = "WITH RECURSIVE node_tree AS (" +
                   "SELECT * FROM organization_nodes WHERE id = :nodeId " +
                   "UNION ALL " +
                   "SELECT n.* FROM organization_nodes n " +
                   "INNER JOIN node_tree nt ON n.parent_id = nt.id " +
                   ") SELECT * FROM node_tree WHERE id != :nodeId", 
           nativeQuery = true)
    List<OrganizationNode> findAllChildrenRecursively(@Param("nodeId") Long nodeId);
    
    /**
     * 查找所有父节点（递归查询）
     */
    @Query(value = "WITH RECURSIVE parent_tree AS (" +
                   "SELECT * FROM organization_nodes WHERE id = :nodeId " +
                   "UNION ALL " +
                   "SELECT n.* FROM organization_nodes n " +
                   "INNER JOIN parent_tree pt ON n.id = pt.parent_id " +
                   ") SELECT * FROM parent_tree WHERE id != :nodeId", 
           nativeQuery = true)
    List<OrganizationNode> findAllParentsRecursively(@Param("nodeId") Long nodeId);
    
    /**
     * 统计子节点数量
     */
    @Query("SELECT COUNT(n) FROM OrganizationNode n WHERE n.parentId = :parentId")
    Long countChildrenByParentId(@Param("parentId") Long parentId);
    
    /**
     * 统计根节点数量
     */
    @Query("SELECT COUNT(n) FROM OrganizationNode n WHERE n.parentId IS NULL")
    Long countRootNodes();
}
