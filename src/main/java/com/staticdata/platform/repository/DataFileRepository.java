package com.staticdata.platform.repository;

import com.staticdata.platform.entity.DataFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataFileRepository extends JpaRepository<DataFile, Long> {

    // Find Data Files by Organization Node ID
    List<DataFile> findByOrganizationNodeIdOrderByCreatedAtDesc(Long organizationNodeId);

    // Find Data Files by Owner ID
    List<DataFile> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    // Find Data Files by Access Level
    List<DataFile> findByAccessLevelOrderByCreatedAtDesc(DataFile.AccessLevel accessLevel);

    // Search by file name using custom query to avoid PostgreSQL bytea issues
    @Query("SELECT df FROM DataFile df WHERE LOWER(df.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY df.createdAt DESC")
    List<DataFile> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(@Param("name") String name);

    // Find by file hash
    Optional<DataFile> findByFileHash(String fileHash);

    // Check if file name is unique under the same Organization Node
    boolean existsByNameAndOrganizationNodeId(String name, Long organizationNodeId);

    // Check if file name is unique under the same Organization Node (excluding specified ID)
    boolean existsByNameAndOrganizationNodeIdAndIdIsNot(String name, Long organizationNodeId,
            Long id);

    // Paginated query for Data Files - simplified query to avoid PostgreSQL issues
    @Query("SELECT df FROM DataFile df")
    Page<DataFile> findAllDataFiles(Pageable pageable);

    // Count Data Files under Organization Node
    long countByOrganizationNodeId(Long organizationNodeId);

    // Count Data Files owned by User
    long countByOwnerId(Long ownerId);

    // Count Data Files by Access Level
    long countByAccessLevel(DataFile.AccessLevel accessLevel);

    // Query by Data Type (via JSON query) - temporarily return all files, filter in Service layer
    @Query("SELECT df FROM DataFile df")
    List<DataFile> findAllDataFiles();

    // Find recently created Data Files
    @Query("SELECT df FROM DataFile df ORDER BY df.createdAt DESC")
    List<DataFile> findRecentDataFiles(Pageable pageable);

    // Find Data Files by organization path
    @Query("SELECT df FROM DataFile df " + "JOIN df.organizationNode on "
            + "WHERE on.id = :organizationNodeId OR on.parentId = :organizationNodeId "
            + "ORDER BY df.createdAt DESC")
    List<DataFile> findByOrganizationNodeAndChildren(
            @Param("organizationNodeId") Long organizationNodeId);

    // GetData FileStatistics
    @Query("SELECT " + "COUNT(df), "
            + "SUM(CASE WHEN df.accessLevel = 'PUBLIC' THEN 1 ELSE 0 END), "
            + "SUM(CASE WHEN df.accessLevel = 'PRIVATE' THEN 1 ELSE 0 END), "
            + "COALESCE(AVG(CAST(df.rowCount AS DOUBLE)), 0.0), "
            + "COALESCE(AVG(CAST(df.columnCount AS DOUBLE)), 0.0) " + "FROM DataFile df")
    Object[] getDataFileStatistics();

    // Get user accessible data files (including public and owned)
    @Query("SELECT df FROM DataFile df WHERE "
            + "df.accessLevel = 'PUBLIC' OR df.owner.id = :userId " + "ORDER BY df.createdAt DESC")
    List<DataFile> findAccessibleByUser(@Param("userId") Long userId, Pageable pageable);
}
