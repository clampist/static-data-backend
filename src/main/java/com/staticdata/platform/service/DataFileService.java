package com.staticdata.platform.service;

import com.staticdata.platform.dto.*;
import com.staticdata.platform.entity.DataFile;
import com.staticdata.platform.entity.OrganizationNode;
import com.staticdata.platform.entity.User;
import com.staticdata.platform.exception.BusinessException;
import com.staticdata.platform.exception.ResourceNotFoundException;
import com.staticdata.platform.repository.DataFileRepository;
import com.staticdata.platform.repository.OrganizationNodeRepository;
import com.staticdata.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataFileService {

  private final DataFileRepository dataFileRepository;
  private final OrganizationNodeRepository organizationNodeRepository;
  private final UserRepository userRepository;

  @Transactional
  public DataFileDto createDataFile(CreateDataFileRequest request) {
    log.info("Creating data file: {}", request.getName());

    // 验证组织节点是否存在
    OrganizationNode organizationNode =
        organizationNodeRepository.findById(request.getOrganizationNodeId()).orElseThrow(
            () -> new ResourceNotFoundException("组织节点不存在，ID: " + request.getOrganizationNodeId()));

    // 验证模块类型（数据文件只能挂在MODULE类型的节点下）
    if (organizationNode.getType() != OrganizationNode.NodeType.MODULE) {
      throw new BusinessException("数据文件只能挂在功能模块下，当前节点类型为: " + organizationNode.getType());
    }

    // 检查文件名在同一组织节点下是否唯一
    if (dataFileRepository.existsByNameAndOrganizationNodeId(request.getName(),
        request.getOrganizationNodeId())) {
      throw new BusinessException("在同一模块下，数据文件名已存在: " + request.getName());
    }

    // 获取当前用户
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    User owner = userRepository.findByUsername(currentUsername)
        .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + currentUsername));

    // 创建数据文件实体
    DataFile dataFile = new DataFile();
    dataFile.setName(request.getName());
    dataFile.setDescription(request.getDescription());
    dataFile.setOrganizationNode(organizationNode);
    dataFile.setOwner(owner);
    dataFile.setAccessLevel(request.getAccessLevel());

    // 处理列定义
    if (request.getColumnDefinitions() != null) {
      List<DataFile.ColumnDefinition> columnDefinitions = request.getColumnDefinitions().stream()
          .map(this::convertToColumnDefinition).collect(Collectors.toList());
      dataFile.setColumnDefinitions(columnDefinitions);
      dataFile.setColumnCount(columnDefinitions.size());
    }

    // 处理数据行
    if (request.getDataRows() != null) {
      dataFile.setDataRows(request.getDataRows());
      dataFile.setRowCount(request.getDataRows().size());
    }

    // 生成文件哈希
    String fileHash = generateFileHash(dataFile);
    dataFile.setFileHash(fileHash);

    // 设置审计信息
    dataFile.setCreatedBy(currentUsername);
    dataFile.setUpdatedBy(currentUsername);

    DataFile savedDataFile = dataFileRepository.save(dataFile);
    log.info("Data file created with ID: {}", savedDataFile.getId());

    return convertToDto(savedDataFile);
  }

  @Transactional
  public DataFileDto updateDataFile(Long id, UpdateDataFileRequest request) {
    log.info("Updating data file with ID: {}", id);
    DataFile existingDataFile = dataFileRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("数据文件不存在，ID: " + id));

    // 检查权限（只有所有者可以修改）
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    if (!existingDataFile.getOwner().getUsername().equals(currentUsername)) {
      throw new BusinessException("只有文件所有者可以修改数据文件");
    }

    // 更新基本信息
    if (request.getName() != null && !request.getName().equals(existingDataFile.getName())) {
      if (dataFileRepository.existsByNameAndOrganizationNodeIdAndIdIsNot(request.getName(),
          existingDataFile.getOrganizationNode().getId(), id)) {
        throw new BusinessException("在同一模块下，数据文件名已存在: " + request.getName());
      }
      existingDataFile.setName(request.getName());
    }

    if (request.getDescription() != null) {
      existingDataFile.setDescription(request.getDescription());
    }

    if (request.getAccessLevel() != null) {
      existingDataFile.setAccessLevel(request.getAccessLevel());
    }

    // 更新列定义
    if (request.getColumnDefinitions() != null) {
      List<DataFile.ColumnDefinition> columnDefinitions = request.getColumnDefinitions().stream()
          .map(this::convertToColumnDefinition).collect(Collectors.toList());
      existingDataFile.setColumnDefinitions(columnDefinitions);
      existingDataFile.setColumnCount(columnDefinitions.size());
    }

    // 更新数据行
    if (request.getDataRows() != null) {
      existingDataFile.setDataRows(request.getDataRows());
      existingDataFile.setRowCount(request.getDataRows().size());
    }

    // 重新生成文件哈希
    String newFileHash = generateFileHash(existingDataFile);
    existingDataFile.setFileHash(newFileHash);

    // 更新审计信息
    existingDataFile.setUpdatedBy(currentUsername);
    existingDataFile.setUpdatedAt(LocalDateTime.now());

    DataFile updatedDataFile = dataFileRepository.save(existingDataFile);
    log.info("Data file updated with ID: {}", updatedDataFile.getId());

    return convertToDto(updatedDataFile);
  }

  @Transactional
  public void deleteDataFile(Long id) {
    log.info("Deleting data file with ID: {}", id);
    DataFile existingDataFile = dataFileRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("数据文件不存在，ID: " + id));

    // 检查权限（只有所有者可以删除）
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    if (!existingDataFile.getOwner().getUsername().equals(currentUsername)) {
      throw new BusinessException("只有文件所有者可以删除数据文件");
    }

    dataFileRepository.delete(existingDataFile);
    log.info("Data file deleted with ID: {}", id);
  }

  @Transactional(readOnly = true)
  public DataFileDto getDataFileById(Long id) {
    log.debug("Fetching data file by ID: {}", id);
    DataFile dataFile = dataFileRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("数据文件不存在，ID: " + id));

    // 检查访问权限
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    User currentUser = userRepository.findByUsername(currentUsername)
        .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + currentUsername));

    if (dataFile.getAccessLevel() == DataFile.AccessLevel.PRIVATE
        && !dataFile.getOwner().getId().equals(currentUser.getId())) {
      throw new BusinessException("没有权限访问此数据文件");
    }

    return convertToDto(dataFile);
  }

  @Transactional(readOnly = true)
  public Page<DataFileDto> queryDataFiles(DataFileQueryRequest request) {
    log.debug("Querying data files with conditions: {}", request);

    // 构建分页和排序
    Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
    Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), sort);

    // 执行查询 - 使用最简单的查询避免PostgreSQL问题
    Page<DataFile> dataFiles = dataFileRepository.findAll(pageable);

    // 过滤用户可访问的文件
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    User currentUser = userRepository.findByUsername(currentUsername)
        .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + currentUsername));

    List<DataFileDto> accessibleFiles = dataFiles.getContent().stream()
        .filter(df -> df.getAccessLevel() == DataFile.AccessLevel.PUBLIC
            || df.getOwner().getId().equals(currentUser.getId()))
        .map(this::convertToDto).collect(Collectors.toList());

    return new org.springframework.data.domain.PageImpl<>(accessibleFiles, pageable,
        accessibleFiles.size());
  }

  @Transactional(readOnly = true)
  public List<DataFileDto> getDataFilesByOrganizationNode(Long organizationNodeId) {
    log.debug("Fetching data files for organization node ID: {}", organizationNodeId);
    List<DataFile> dataFiles =
        dataFileRepository.findByOrganizationNodeIdOrderByCreatedAtDesc(organizationNodeId);

    // 过滤用户可访问的文件
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    User currentUser = userRepository.findByUsername(currentUsername)
        .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + currentUsername));

    return dataFiles.stream()
        .filter(df -> df.getAccessLevel() == DataFile.AccessLevel.PUBLIC
            || df.getOwner().getId().equals(currentUser.getId()))
        .map(this::convertToDto).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<DataFileDto> getDataFilesByOwner(Long ownerId) {
    log.debug("Fetching data files for owner ID: {}", ownerId);
    List<DataFile> dataFiles = dataFileRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
    return dataFiles.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<DataFileDto> searchDataFiles(String keyword) {
    log.debug("Searching data files with keyword: {}", keyword);
    List<DataFile> dataFiles =
        dataFileRepository.findByNameContainingIgnoreCaseOrderByCreatedAtDesc(keyword);

    // 过滤用户可访问的文件
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    User currentUser = userRepository.findByUsername(currentUsername)
        .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + currentUsername));

    return dataFiles.stream()
        .filter(df -> df.getAccessLevel() == DataFile.AccessLevel.PUBLIC
            || df.getOwner().getId().equals(currentUser.getId()))
        .map(this::convertToDto).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<DataFileDto> getDataFilesByDataType(DataFile.ColumnDefinition.DataType dataType) {
    log.debug("Fetching data files by data type: {}", dataType);
    List<DataFile> allDataFiles = dataFileRepository.findAllDataFiles();

    // 过滤用户可访问的文件和包含指定数据类型的文件
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    User currentUser = userRepository.findByUsername(currentUsername)
        .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + currentUsername));

    return allDataFiles.stream()
        .filter(df -> df.getAccessLevel() == DataFile.AccessLevel.PUBLIC
            || df.getOwner().getId().equals(currentUser.getId()))
        .filter(df -> df.getColumnDefinitions() != null
            && df.getColumnDefinitions().stream().anyMatch(cd -> cd.getDataType() == dataType))
        .map(this::convertToDto).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<DataFileDto> getRecentDataFiles(int limit) {
    log.debug("Fetching recent data files with limit: {}", limit);
    Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
    List<DataFile> dataFiles = dataFileRepository.findRecentDataFiles(pageable);

    // 过滤用户可访问的文件
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    User currentUser = userRepository.findByUsername(currentUsername)
        .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + currentUsername));

    return dataFiles.stream()
        .filter(df -> df.getAccessLevel() == DataFile.AccessLevel.PUBLIC
            || df.getOwner().getId().equals(currentUser.getId()))
        .map(this::convertToDto).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public Map<String, Object> getDataFileStatistics() {
    log.debug("Fetching data file statistics");

    // 直接使用Repository方法计算统计信息，避免复杂的查询
    long totalFiles = dataFileRepository.count();
    long publicFiles = dataFileRepository.countByAccessLevel(DataFile.AccessLevel.PUBLIC);
    long privateFiles = dataFileRepository.countByAccessLevel(DataFile.AccessLevel.PRIVATE);

    // 计算平均行数和列数
    List<DataFile> allFiles = dataFileRepository.findAll();
    double avgRowCount = allFiles.isEmpty() ? 0.0
        : allFiles.stream().mapToInt(df -> df.getRowCount() != null ? df.getRowCount() : 0)
            .average().orElse(0.0);
    double avgColumnCount = allFiles.isEmpty() ? 0.0
        : allFiles.stream().mapToInt(df -> df.getColumnCount() != null ? df.getColumnCount() : 0)
            .average().orElse(0.0);

    Map<String, Object> statistics = new HashMap<>();
    statistics.put("totalFiles", totalFiles);
    statistics.put("publicFiles", publicFiles);
    statistics.put("privateFiles", privateFiles);
    statistics.put("avgRowCount", avgRowCount);
    statistics.put("avgColumnCount", avgColumnCount);

    return statistics;
  }

  // 私有辅助方法
  private DataFile.ColumnDefinition convertToColumnDefinition(
      CreateDataFileRequest.ColumnDefinitionRequest request) {
    DataFile.ColumnDefinition columnDef = new DataFile.ColumnDefinition();
    columnDef.setName(request.getName());
    columnDef.setDataType(request.getDataType());
    columnDef.setRequired(request.getRequired());
    columnDef.setDefaultValue(request.getDefaultValue());
    columnDef.setMaxLength(request.getMaxLength());
    return columnDef;
  }

  private DataFile.ColumnDefinition convertToColumnDefinition(
      UpdateDataFileRequest.ColumnDefinitionRequest request) {
    DataFile.ColumnDefinition columnDef = new DataFile.ColumnDefinition();
    columnDef.setName(request.getName());
    columnDef.setDataType(request.getDataType());
    columnDef.setRequired(request.getRequired());
    columnDef.setDefaultValue(request.getDefaultValue());
    columnDef.setMaxLength(request.getMaxLength());
    return columnDef;
  }

  private String generateFileHash(DataFile dataFile) {
    try {
      String content = dataFile.getName() + dataFile.getDescription()
          + dataFile.getColumnDefinitions().toString() + dataFile.getDataRows().toString();

      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] hashBytes = md.digest(content.getBytes());

      StringBuilder sb = new StringBuilder();
      for (byte b : hashBytes) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Error generating file hash", e);
    }
  }

  private String buildOrganizationPath(OrganizationNode node) {
    List<String> pathParts = new ArrayList<>();
    OrganizationNode current = node;

    while (current != null) {
      pathParts.add(0, current.getName());
      current = current.getParentId() != null
          ? organizationNodeRepository.findById(current.getParentId()).orElse(null)
          : null;
    }

    return String.join("/", pathParts);
  }

  private DataFileDto convertToDto(DataFile dataFile) {
    return DataFileDto.builder().id(dataFile.getId()).name(dataFile.getName())
        .description(dataFile.getDescription()).fileHash(dataFile.getFileHash())
        .organizationNodeId(dataFile.getOrganizationNode().getId())
        .organizationNodeName(dataFile.getOrganizationNode().getName())
        .organizationNodePath(buildOrganizationPath(dataFile.getOrganizationNode()))
        .ownerId(dataFile.getOwner().getId())
        .ownerName(dataFile.getOwner().getFullName() != null ? dataFile.getOwner().getFullName()
            : dataFile.getOwner().getUsername())
        .accessLevel(dataFile.getAccessLevel())
        .columnDefinitions(convertColumnDefinitionsToDto(dataFile.getColumnDefinitions()))
        .dataRows(dataFile.getDataRows()).rowCount(dataFile.getRowCount())
        .columnCount(dataFile.getColumnCount()).createdAt(dataFile.getCreatedAt())
        .updatedAt(dataFile.getUpdatedAt()).createdBy(dataFile.getCreatedBy())
        .updatedBy(dataFile.getUpdatedBy())
        .versionCount(dataFile.getVersions() != null ? dataFile.getVersions().size() : 0)
        .lastModifiedBy(dataFile.getUpdatedBy()).lastModifiedAt(dataFile.getUpdatedAt()).build();
  }

  private List<DataFileDto.ColumnDefinitionDto> convertColumnDefinitionsToDto(
      List<DataFile.ColumnDefinition> columnDefinitions) {
    if (columnDefinitions == null) {
      return new ArrayList<>();
    }

    return columnDefinitions.stream()
        .map(cd -> DataFileDto.ColumnDefinitionDto.builder().name(cd.getName())
            .dataType(cd.getDataType()).required(cd.getRequired())
            .defaultValue(cd.getDefaultValue()).maxLength(cd.getMaxLength()).build())
        .collect(Collectors.toList());
  }
}
