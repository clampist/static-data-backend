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

    // Validate if organization node exists
    OrganizationNode organizationNode =
        organizationNodeRepository.findById(request.getOrganizationNodeId()).orElseThrow(
            () -> new ResourceNotFoundException("Organization node does not exist, ID: " + request.getOrganizationNodeId()));

    // Validate module type (data files can only be attached to MODULE type nodes)
    if (organizationNode.getType() != OrganizationNode.NodeType.MODULE) {
      throw new BusinessException("Data file can only be attached to functional modules, current node type is: " + organizationNode.getType());
    }

    // Check if file name is unique under the same Organization Node
    if (dataFileRepository.existsByNameAndOrganizationNodeId(request.getName(),
        request.getOrganizationNodeId())) {
      throw new BusinessException("Data file name already exists under the same module: " + request.getName());
    }

    // GetCurrentUser
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    User owner = userRepository.findByUsername(currentUsername)
        .orElseThrow(() -> new ResourceNotFoundException("UserDoes not exist: " + currentUsername));

    // CreateData FileEntity
    DataFile dataFile = new DataFile();
    dataFile.setName(request.getName());
    dataFile.setDescription(request.getDescription());
    dataFile.setOrganizationNode(organizationNode);
    dataFile.setOwner(owner);
    dataFile.setAccessLevel(request.getAccessLevel());

    // Handle column definitions
    if (request.getColumnDefinitions() != null) {
      List<DataFile.ColumnDefinition> columnDefinitions = request.getColumnDefinitions().stream()
          .map(this::convertToColumnDefinition).collect(Collectors.toList());
      dataFile.setColumnDefinitions(columnDefinitions);
      dataFile.setColumnCount(columnDefinitions.size());
    }

    // HandleDataRow
    if (request.getDataRows() != null) {
      dataFile.setDataRows(request.getDataRows());
      dataFile.setRowCount(request.getDataRows().size());
    }

    // GenerateFileHash
    String fileHash = generateFileHash(dataFile);
    dataFile.setFileHash(fileHash);

    // Set audit information
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
        .orElseThrow(() -> new ResourceNotFoundException("Data file does not exist, ID: " + id));

    // Check permission (only file owner can modify)
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    if (!existingDataFile.getOwner().getUsername().equals(currentUsername)) {
      throw new BusinessException("Only file owner can modify data file");
    }

    // Update basic information
    if (request.getName() != null && !request.getName().equals(existingDataFile.getName())) {
      if (dataFileRepository.existsByNameAndOrganizationNodeIdAndIdIsNot(request.getName(),
          existingDataFile.getOrganizationNode().getId(), id)) {
        throw new BusinessException("Data file name already exists under the same module: " + request.getName());
      }
      existingDataFile.setName(request.getName());
    }

    if (request.getDescription() != null) {
      existingDataFile.setDescription(request.getDescription());
    }

    if (request.getAccessLevel() != null) {
      existingDataFile.setAccessLevel(request.getAccessLevel());
    }

    // Update column definitions
    if (request.getColumnDefinitions() != null) {
      List<DataFile.ColumnDefinition> columnDefinitions = request.getColumnDefinitions().stream()
          .map(this::convertToColumnDefinition).collect(Collectors.toList());
      existingDataFile.setColumnDefinitions(columnDefinitions);
      existingDataFile.setColumnCount(columnDefinitions.size());
    }

    // UpdateDataRow
    if (request.getDataRows() != null) {
      existingDataFile.setDataRows(request.getDataRows());
      existingDataFile.setRowCount(request.getDataRows().size());
    }

    // Regenerate file hash
    String newFileHash = generateFileHash(existingDataFile);
    existingDataFile.setFileHash(newFileHash);

    // Update audit information
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
        .orElseThrow(() -> new ResourceNotFoundException("Data file does not exist, ID: " + id));

    // Check permission (only file owner can delete)
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    if (!existingDataFile.getOwner().getUsername().equals(currentUsername)) {
      throw new BusinessException("Only file owner can delete data file");
    }

    dataFileRepository.delete(existingDataFile);
    log.info("Data file deleted with ID: {}", id);
  }

  @Transactional(readOnly = true)
  public DataFileDto getDataFileById(Long id) {
    log.debug("Fetching data file by ID: {}", id);
    DataFile dataFile = dataFileRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Data file does not exist, ID: " + id));

    // Check access permission
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    User currentUser = userRepository.findByUsername(currentUsername)
        .orElseThrow(() -> new ResourceNotFoundException("UserDoes not exist: " + currentUsername));

    if (dataFile.getAccessLevel() == DataFile.AccessLevel.PRIVATE
        && !dataFile.getOwner().getId().equals(currentUser.getId())) {
      throw new BusinessException("No permission to access this data file");
    }

    return convertToDto(dataFile);
  }

  @Transactional(readOnly = true)
  public Page<DataFileDto> queryDataFiles(DataFileQueryRequest request) {
    log.debug("Querying data files with conditions: {}", request);

    // Build pagination and sort
    Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
    Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), sort);

    // Execute query - use simplest query to avoid PostgreSQL issues
    Page<DataFile> dataFiles = dataFileRepository.findAll(pageable);

    // Filter files accessible by user
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    User currentUser = userRepository.findByUsername(currentUsername)
        .orElseThrow(() -> new ResourceNotFoundException("UserDoes not exist: " + currentUsername));

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

    // Filter files accessible by user
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    User currentUser = userRepository.findByUsername(currentUsername)
        .orElseThrow(() -> new ResourceNotFoundException("UserDoes not exist: " + currentUsername));

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

    // Filter files accessible by user
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    User currentUser = userRepository.findByUsername(currentUsername)
        .orElseThrow(() -> new ResourceNotFoundException("UserDoes not exist: " + currentUsername));

    return dataFiles.stream()
        .filter(df -> df.getAccessLevel() == DataFile.AccessLevel.PUBLIC
            || df.getOwner().getId().equals(currentUser.getId()))
        .map(this::convertToDto).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<DataFileDto> getDataFilesByDataType(DataFile.ColumnDefinition.DataType dataType) {
    log.debug("Fetching data files by data type: {}", dataType);
    List<DataFile> allDataFiles = dataFileRepository.findAllDataFiles();

    // Filter files accessible by user and containing specified data type
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    User currentUser = userRepository.findByUsername(currentUsername)
        .orElseThrow(() -> new ResourceNotFoundException("UserDoes not exist: " + currentUsername));

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

    // Filter files accessible by user
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    User currentUser = userRepository.findByUsername(currentUsername)
        .orElseThrow(() -> new ResourceNotFoundException("UserDoes not exist: " + currentUsername));

    return dataFiles.stream()
        .filter(df -> df.getAccessLevel() == DataFile.AccessLevel.PUBLIC
            || df.getOwner().getId().equals(currentUser.getId()))
        .map(this::convertToDto).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public Map<String, Object> getDataFileStatistics() {
    log.debug("Fetching data file statistics");

    // Use repository methods directly to calculate statistics, avoid complex queries
    long totalFiles = dataFileRepository.count();
    long publicFiles = dataFileRepository.countByAccessLevel(DataFile.AccessLevel.PUBLIC);
    long privateFiles = dataFileRepository.countByAccessLevel(DataFile.AccessLevel.PRIVATE);

    // Calculate average row count and column count
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

  // Private helper methods
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
