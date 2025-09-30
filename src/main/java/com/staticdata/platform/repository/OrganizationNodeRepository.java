package com.staticdata.platform.repository;

import com.staticdata.platform.entity.OrganizationNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Organization NodeRepository
 */
@Repository
public interface OrganizationNodeRepository extends JpaRepository<OrganizationNode, Long> {
    
    /**
     * Find child nodes by parent node ID
     */
    List<OrganizationNode> findByParentIdOrderBySortOrderAsc(Long parentId);
    
    /**
     * Find root nodes (parent node ID is null)
     */
    List<OrganizationNode> findByParentIdIsNullOrderBySortOrderAsc();
    
    /**
     * Find nodes by node type
     */
    List<OrganizationNode> findByTypeOrderBySortOrderAsc(OrganizationNode.NodeType type);
    
    /**
     * Find by name fuzzy query
     */
    @Query("SELECT n FROM OrganizationNode n WHERE n.name LIKE %:name% ORDER BY n.sortOrder ASC")
    List<OrganizationNode> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Check if nodes with same name exist under same parent node
     */
    @Query("SELECT COUNT(n) > 0 FROM OrganizationNode n WHERE n.name = :name AND n.parentId = :parentId AND n.id != :excludeId")
    boolean existsByNameAndParentIdAndIdNot(@Param("name") String name, 
                                           @Param("parentId") Long parentId, 
                                           @Param("excludeId") Long excludeId);
    
    /**
     * Check if nodes with same name exist under root node
     */
    @Query("SELECT COUNT(n) > 0 FROM OrganizationNode n WHERE n.name = :name AND n.parentId IS NULL AND n.id != :excludeId")
    boolean existsByNameAndParentIdIsNullAndIdNot(@Param("name") String name, 
                                                 @Param("excludeId") Long excludeId);
    
    /**
     * Find all child nodes (recursive query)
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
     * Find all parent nodes (recursive query)
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
     * Count child nodes
     */
    @Query("SELECT COUNT(n) FROM OrganizationNode n WHERE n.parentId = :parentId")
    Long countChildrenByParentId(@Param("parentId") Long parentId);
    
    /**
     * Count root nodes
     */
    @Query("SELECT COUNT(n) FROM OrganizationNode n WHERE n.parentId IS NULL")
    Long countRootNodes();
}
