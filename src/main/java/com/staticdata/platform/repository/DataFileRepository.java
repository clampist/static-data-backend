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

  // 根据组织节点ID查找数据文件
  List<DataFile> findByOrganizationNodeIdOrderByCreatedAtDesc(Long organizationNodeId);

  // 根据所有者ID查找数据文件
  List<DataFile> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

  // 根据访问级别查找数据文件
  List<DataFile> findByAccessLevelOrderByCreatedAtDesc(DataFile.AccessLevel accessLevel);

  // 根据文件名模糊查询
  List<DataFile> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name);

  // 根据文件哈希查找
  Optional<DataFile> findByFileHash(String fileHash);

  // 检查文件名在同一组织节点下是否唯一
  boolean existsByNameAndOrganizationNodeId(String name, Long organizationNodeId);

  // 检查文件名在同一组织节点下是否唯一（排除指定ID）
  boolean existsByNameAndOrganizationNodeIdAndIdIsNot(String name, Long organizationNodeId,
      Long id);

  // 分页查询数据文件
  @Query("SELECT df FROM DataFile df WHERE "
      + "(:name IS NULL OR LOWER(df.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND "
      + "(:organizationNodeId IS NULL OR df.organizationNode.id = :organizationNodeId) AND "
      + "(:ownerId IS NULL OR df.owner.id = :ownerId) AND "
      + "(:accessLevel IS NULL OR df.accessLevel = :accessLevel)")
  Page<DataFile> findByConditions(@Param("name") String name,
      @Param("organizationNodeId") Long organizationNodeId, @Param("ownerId") Long ownerId,
      @Param("accessLevel") DataFile.AccessLevel accessLevel, Pageable pageable);

  // 统计组织节点下的数据文件数量
  long countByOrganizationNodeId(Long organizationNodeId);

  // 统计用户拥有的数据文件数量
  long countByOwnerId(Long ownerId);

  // 根据数据类型查询（通过JSON查询）- 暂时返回所有文件，在Service层过滤
  @Query("SELECT df FROM DataFile df")
  List<DataFile> findAllDataFiles();

  // 查找最近创建的数据文件
  @Query("SELECT df FROM DataFile df ORDER BY df.createdAt DESC")
  List<DataFile> findRecentDataFiles(Pageable pageable);

  // 根据组织路径查找数据文件
  @Query("SELECT df FROM DataFile df " + "JOIN df.organizationNode on "
      + "WHERE on.id = :organizationNodeId OR on.parentId = :organizationNodeId "
      + "ORDER BY df.createdAt DESC")
  List<DataFile> findByOrganizationNodeAndChildren(
      @Param("organizationNodeId") Long organizationNodeId);

  // 获取数据文件统计信息
  @Query("SELECT " + "COUNT(df) as totalFiles, "
      + "SUM(CASE WHEN df.accessLevel = 'PUBLIC' THEN 1 ELSE 0 END) as publicFiles, "
      + "SUM(CASE WHEN df.accessLevel = 'PRIVATE' THEN 1 ELSE 0 END) as privateFiles, "
      + "AVG(CAST(df.rowCount AS DOUBLE)) as avgRowCount, "
      + "AVG(CAST(df.columnCount AS DOUBLE)) as avgColumnCount " + "FROM DataFile df")
  Object[] getDataFileStatistics();

  // 获取用户可访问的数据文件（包括公开的和自己拥有的）
  @Query("SELECT df FROM DataFile df WHERE " + "df.accessLevel = 'PUBLIC' OR df.owner.id = :userId "
      + "ORDER BY df.createdAt DESC")
  List<DataFile> findAccessibleByUser(@Param("userId") Long userId, Pageable pageable);
}
