package com.staticdata.platform.service;

import com.staticdata.platform.dto.CreateOrganizationNodeRequest;
import com.staticdata.platform.dto.OrganizationNodeDto;
import com.staticdata.platform.dto.UpdateOrganizationNodeRequest;
import com.staticdata.platform.entity.OrganizationNode;
import com.staticdata.platform.entity.User;
import com.staticdata.platform.exception.ResourceNotFoundException;
import com.staticdata.platform.exception.BusinessException;
import com.staticdata.platform.repository.OrganizationNodeRepository;
import com.staticdata.platform.repository.UserRepository;
import com.staticdata.platform.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Organization management service class
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationService {

    private final OrganizationNodeRepository organizationNodeRepository;
    private final UserRepository userRepository;

    /**
     * Get complete organization tree
     */
    @Transactional(readOnly = true)
    public List<OrganizationNodeDto> getOrganizationTree() {
        log.info("Getting organization tree");

        // Get all nodes
        List<OrganizationNode> allNodes = organizationNodeRepository.findAll();

        if (allNodes.isEmpty()) {
            return new ArrayList<>();
        }

        // Build tree structure
        return buildTree(allNodes);
    }

    /**
     * Get child nodes by parent node ID
     */
    @Transactional(readOnly = true)
    public List<OrganizationNodeDto> getChildrenByParentId(Long parentId) {
        log.info("Getting children for parent node: {}", parentId);

        List<OrganizationNode> children;
        if (parentId == null) {
            children = organizationNodeRepository.findByParentIdIsNullOrderBySortOrderAsc();
        } else {
            children = organizationNodeRepository.findByParentIdOrderBySortOrderAsc(parentId);
        }

        return children.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * Get node details by ID
     */
    @Transactional(readOnly = true)
    public OrganizationNodeDto getNodeById(Long id) {
        log.info("Getting organization node by id: {}", id);

        OrganizationNode node = organizationNodeRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Organization NodeDoes not exist: " + id));

        return convertToDto(node);
    }

    /**
     * Create organization node
     */
    @Transactional
    public OrganizationNodeDto createNode(CreateOrganizationNodeRequest request) {
        log.info("Creating organization node: {}", request.getName());

        // Validate if parent node exists
        if (request.getParentId() != null) {
            organizationNodeRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent node does not exist: " + request.getParentId()));
        }

        // Check if name is duplicate
        if (isNameDuplicate(request.getName(), request.getParentId(), null)) {
            throw new BusinessException(
                    "Node with same name already exists at same level: " + request.getName());
        }

        // GetCurrentUser
        String currentUser = getCurrentUsername();

        // Create new node
        OrganizationNode node = new OrganizationNode();
        node.setName(request.getName());
        node.setDescription(request.getDescription());
        node.setType(request.getType());
        node.setParentId(request.getParentId());
        node.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        node.setCreatedBy(currentUser);
        node.setUpdatedBy(currentUser);

        OrganizationNode savedNode = organizationNodeRepository.save(node);

        log.info("Created organization node: {} with id: {}", savedNode.getName(),
                savedNode.getId());

        return convertToDto(savedNode);
    }

    /**
     * Update organization node
     */
    @Transactional
    public OrganizationNodeDto updateNode(Long id, UpdateOrganizationNodeRequest request) {
        log.info("Updating organization node: {}", id);

        OrganizationNode node = organizationNodeRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Organization NodeDoes not exist: " + id));

        // Check if name is duplicate
        if (isNameDuplicate(request.getName(), node.getParentId(), id)) {
            throw new BusinessException(
                    "Node with same name already exists at same level: " + request.getName());
        }

        // GetCurrentUser
        String currentUser = getCurrentUsername();

        // Update node information
        node.setName(request.getName());
        node.setDescription(request.getDescription());
        if (request.getSortOrder() != null) {
            node.setSortOrder(request.getSortOrder());
        }
        node.setUpdatedBy(currentUser);

        OrganizationNode updatedNode = organizationNodeRepository.save(node);

        log.info("Updated organization node: {}", updatedNode.getName());

        return convertToDto(updatedNode);
    }

    /**
     * Delete organization node
     */
    @Transactional
    public void deleteNode(Long id) {
        log.info("Deleting organization node: {}", id);

        OrganizationNode node = organizationNodeRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Organization NodeDoes not exist: " + id));

        // Check if has child nodes
        List<OrganizationNode> children =
                organizationNodeRepository.findByParentIdOrderBySortOrderAsc(id);
        if (!children.isEmpty()) {
            throw new BusinessException(
                    "Cannot delete node containing child nodes, please delete all child nodes first");
        }

        // Check if has associated data files
        // TODO: Add data file association check

        organizationNodeRepository.delete(node);

        log.info("Deleted organization node: {}", node.getName());
    }

    /**
     * Search organization nodes
     */
    @Transactional(readOnly = true)
    public List<OrganizationNodeDto> searchNodes(String keyword) {
        log.info("Searching organization nodes with keyword: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            return getOrganizationTree();
        }

        List<OrganizationNode> nodes =
                organizationNodeRepository.findByNameContainingIgnoreCase(keyword.trim());

        return nodes.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * Move node to new parent node
     */
    @Transactional
    public OrganizationNodeDto moveNode(Long nodeId, Long newParentId) {
        log.info("Moving node {} to parent {}", nodeId, newParentId);

        OrganizationNode node = organizationNodeRepository.findById(nodeId).orElseThrow(
                () -> new ResourceNotFoundException("Organization NodeDoes not exist: " + nodeId));

        // Validate if new parent node exists
        if (newParentId != null) {
            organizationNodeRepository.findById(newParentId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Target parent node does not exist: " + newParentId));
        }

        // Check if would form circular reference
        if (wouldCreateCircularReference(nodeId, newParentId)) {
            throw new BusinessException("Cannot move node to its child node");
        }

        // Check if new location has duplicate name
        if (isNameDuplicate(node.getName(), newParentId, nodeId)) {
            throw new BusinessException(
                    "Node with same name already exists at target location: " + node.getName());
        }

        // GetCurrentUser
        String currentUser = getCurrentUsername();

        // Update node
        node.setParentId(newParentId);
        node.setUpdatedBy(currentUser);

        OrganizationNode updatedNode = organizationNodeRepository.save(node);

        log.info("Moved node {} to parent {}", nodeId, newParentId);

        return convertToDto(updatedNode);
    }

    /**
     * Build tree structure
     */
    private List<OrganizationNodeDto> buildTree(List<OrganizationNode> nodes) {
        // Create ID to node mapping
        Map<Long, OrganizationNodeDto> nodeMap = nodes.stream().map(this::convertToDto)
                .collect(Collectors.toMap(OrganizationNodeDto::getId, dto -> dto));

        // Build parent-child relationship
        List<OrganizationNodeDto> rootNodes = new ArrayList<>();

        for (OrganizationNodeDto dto : nodeMap.values()) {
            if (dto.getParentId() == null) {
                rootNodes.add(dto);
            } else {
                OrganizationNodeDto parent = nodeMap.get(dto.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dto);
                }
            }
        }

        // Sort child nodes at each level
        sortChildren(rootNodes);

        return rootNodes;
    }

    /**
     * Recursively sort child nodes
     */
    private void sortChildren(List<OrganizationNodeDto> children) {
        if (children != null && !children.isEmpty()) {
            children.sort((a, b) -> {
                int sortOrderCompare =
                        Integer.compare(a.getSortOrder() != null ? a.getSortOrder() : 0,
                                b.getSortOrder() != null ? b.getSortOrder() : 0);
                if (sortOrderCompare == 0) {
                    return a.getName().compareTo(b.getName());
                }
                return sortOrderCompare;
            });

            // Recursively sort each node's child nodes
            children.forEach(child -> sortChildren(child.getChildren()));
        }
    }

    /**
     * Check if name is duplicate
     */
    private boolean isNameDuplicate(String name, Long parentId, Long excludeId) {
        if (excludeId == null) {
            excludeId = -1L; // Use a non-existent ID
        }

        if (parentId == null) {
            return organizationNodeRepository.existsByNameAndParentIdIsNullAndIdNot(name,
                    excludeId);
        } else {
            return organizationNodeRepository.existsByNameAndParentIdAndIdNot(name, parentId,
                    excludeId);
        }
    }

    /**
     * Check if would form circular reference
     */
    private boolean wouldCreateCircularReference(Long nodeId, Long newParentId) {
        if (newParentId == null || newParentId.equals(nodeId)) {
            return false;
        }

        List<OrganizationNode> parents =
                organizationNodeRepository.findAllParentsRecursively(newParentId);
        return parents.stream().anyMatch(parent -> parent.getId().equals(nodeId));
    }

    /**
     * Convert entity to DTO
     */
    private OrganizationNodeDto convertToDto(OrganizationNode node) {
        OrganizationNodeDto dto = OrganizationNodeDto.builder().id(node.getId())
                .name(node.getName()).description(node.getDescription()).type(node.getType())
                .parentId(node.getParentId()).sortOrder(node.getSortOrder())
                .createdAt(node.getCreatedAt()).updatedAt(node.getUpdatedAt())
                .createdBy(node.getCreatedBy()).updatedBy(node.getUpdatedBy()).build();

        // Set parent node name
        if (node.getParentId() != null) {
            organizationNodeRepository.findById(node.getParentId())
                    .ifPresent(parent -> dto.setParentName(parent.getName()));
        }

        // Count child nodes
        dto.setChildrenCount(organizationNodeRepository.countChildrenByParentId(node.getId()));

        // TODO: CountData FileCount
        dto.setDataFilesCount(0L);

        return dto;
    }

    /**
     * GetCurrentUsername
     */
    private String getCurrentUsername() {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            return userPrincipal.getUsername();
        } catch (Exception e) {
            log.warn("Failed to get current user, using SYSTEM as default");
            return "SYSTEM";
        }
    }
}
