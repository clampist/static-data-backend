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
 * 组织管理服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationService {

    private final OrganizationNodeRepository organizationNodeRepository;
    private final UserRepository userRepository;

    /**
     * 获取完整的组织树
     */
    @Transactional(readOnly = true)
    public List<OrganizationNodeDto> getOrganizationTree() {
        log.info("Getting organization tree");
        
        // 获取所有节点
        List<OrganizationNode> allNodes = organizationNodeRepository.findAll();
        
        if (allNodes.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 构建树状结构
        return buildTree(allNodes);
    }

    /**
     * 根据父节点ID获取子节点
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
        
        return children.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取节点详情
     */
    @Transactional(readOnly = true)
    public OrganizationNodeDto getNodeById(Long id) {
        log.info("Getting organization node by id: {}", id);
        
        OrganizationNode node = organizationNodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("组织节点不存在: " + id));
        
        return convertToDto(node);
    }

    /**
     * 创建组织节点
     */
    @Transactional
    public OrganizationNodeDto createNode(CreateOrganizationNodeRequest request) {
        log.info("Creating organization node: {}", request.getName());
        
        // 验证父节点是否存在
        if (request.getParentId() != null) {
            organizationNodeRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("父节点不存在: " + request.getParentId()));
        }
        
        // 检查名称是否重复
        if (isNameDuplicate(request.getName(), request.getParentId(), null)) {
            throw new BusinessException("同一层级下已存在相同名称的节点: " + request.getName());
        }
        
        // 获取当前用户
        String currentUser = getCurrentUsername();
        
        // 创建新节点
        OrganizationNode node = new OrganizationNode();
        node.setName(request.getName());
        node.setDescription(request.getDescription());
        node.setType(request.getType());
        node.setParentId(request.getParentId());
        node.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        node.setCreatedBy(currentUser);
        node.setUpdatedBy(currentUser);
        
        OrganizationNode savedNode = organizationNodeRepository.save(node);
        
        log.info("Created organization node: {} with id: {}", savedNode.getName(), savedNode.getId());
        
        return convertToDto(savedNode);
    }

    /**
     * 更新组织节点
     */
    @Transactional
    public OrganizationNodeDto updateNode(Long id, UpdateOrganizationNodeRequest request) {
        log.info("Updating organization node: {}", id);
        
        OrganizationNode node = organizationNodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("组织节点不存在: " + id));
        
        // 检查名称是否重复
        if (isNameDuplicate(request.getName(), node.getParentId(), id)) {
            throw new BusinessException("同一层级下已存在相同名称的节点: " + request.getName());
        }
        
        // 获取当前用户
        String currentUser = getCurrentUsername();
        
        // 更新节点信息
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
     * 删除组织节点
     */
    @Transactional
    public void deleteNode(Long id) {
        log.info("Deleting organization node: {}", id);
        
        OrganizationNode node = organizationNodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("组织节点不存在: " + id));
        
        // 检查是否有子节点
        List<OrganizationNode> children = organizationNodeRepository.findByParentIdOrderBySortOrderAsc(id);
        if (!children.isEmpty()) {
            throw new BusinessException("无法删除包含子节点的节点，请先删除所有子节点");
        }
        
        // 检查是否有关联的数据文件
        // TODO: 添加数据文件关联检查
        
        organizationNodeRepository.delete(node);
        
        log.info("Deleted organization node: {}", node.getName());
    }

    /**
     * 搜索组织节点
     */
    @Transactional(readOnly = true)
    public List<OrganizationNodeDto> searchNodes(String keyword) {
        log.info("Searching organization nodes with keyword: {}", keyword);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return getOrganizationTree();
        }
        
        List<OrganizationNode> nodes = organizationNodeRepository.findByNameContainingIgnoreCase(keyword.trim());
        
        return nodes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 移动节点到新的父节点下
     */
    @Transactional
    public OrganizationNodeDto moveNode(Long nodeId, Long newParentId) {
        log.info("Moving node {} to parent {}", nodeId, newParentId);
        
        OrganizationNode node = organizationNodeRepository.findById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException("组织节点不存在: " + nodeId));
        
        // 验证新父节点是否存在
        if (newParentId != null) {
            organizationNodeRepository.findById(newParentId)
                    .orElseThrow(() -> new ResourceNotFoundException("目标父节点不存在: " + newParentId));
        }
        
        // 检查是否会形成循环引用
        if (wouldCreateCircularReference(nodeId, newParentId)) {
            throw new BusinessException("不能将节点移动到其子节点下");
        }
        
        // 检查新位置是否有重名
        if (isNameDuplicate(node.getName(), newParentId, nodeId)) {
            throw new BusinessException("目标位置已存在相同名称的节点: " + node.getName());
        }
        
        // 获取当前用户
        String currentUser = getCurrentUsername();
        
        // 更新节点
        node.setParentId(newParentId);
        node.setUpdatedBy(currentUser);
        
        OrganizationNode updatedNode = organizationNodeRepository.save(node);
        
        log.info("Moved node {} to parent {}", nodeId, newParentId);
        
        return convertToDto(updatedNode);
    }

    /**
     * 构建树状结构
     */
    private List<OrganizationNodeDto> buildTree(List<OrganizationNode> nodes) {
        // 创建ID到节点的映射
        Map<Long, OrganizationNodeDto> nodeMap = nodes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toMap(OrganizationNodeDto::getId, dto -> dto));
        
        // 构建父子关系
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
        
        // 对每个层级的子节点进行排序
        sortChildren(rootNodes);
        
        return rootNodes;
    }

    /**
     * 递归排序子节点
     */
    private void sortChildren(List<OrganizationNodeDto> children) {
        if (children != null && !children.isEmpty()) {
            children.sort((a, b) -> {
                int sortOrderCompare = Integer.compare(
                        a.getSortOrder() != null ? a.getSortOrder() : 0,
                        b.getSortOrder() != null ? b.getSortOrder() : 0
                );
                if (sortOrderCompare == 0) {
                    return a.getName().compareTo(b.getName());
                }
                return sortOrderCompare;
            });
            
            // 递归排序每个节点的子节点
            children.forEach(child -> sortChildren(child.getChildren()));
        }
    }

    /**
     * 检查名称是否重复
     */
    private boolean isNameDuplicate(String name, Long parentId, Long excludeId) {
        if (excludeId == null) {
            excludeId = -1L; // 使用一个不存在的ID
        }
        
        if (parentId == null) {
            return organizationNodeRepository.existsByNameAndParentIdIsNullAndIdNot(name, excludeId);
        } else {
            return organizationNodeRepository.existsByNameAndParentIdAndIdNot(name, parentId, excludeId);
        }
    }

    /**
     * 检查是否会形成循环引用
     */
    private boolean wouldCreateCircularReference(Long nodeId, Long newParentId) {
        if (newParentId == null || newParentId.equals(nodeId)) {
            return false;
        }
        
        List<OrganizationNode> parents = organizationNodeRepository.findAllParentsRecursively(newParentId);
        return parents.stream().anyMatch(parent -> parent.getId().equals(nodeId));
    }

    /**
     * 实体转换为DTO
     */
    private OrganizationNodeDto convertToDto(OrganizationNode node) {
        OrganizationNodeDto dto = OrganizationNodeDto.builder()
                .id(node.getId())
                .name(node.getName())
                .description(node.getDescription())
                .type(node.getType())
                .parentId(node.getParentId())
                .sortOrder(node.getSortOrder())
                .createdAt(node.getCreatedAt())
                .updatedAt(node.getUpdatedAt())
                .createdBy(node.getCreatedBy())
                .updatedBy(node.getUpdatedBy())
                .build();
        
        // 设置父节点名称
        if (node.getParentId() != null) {
            organizationNodeRepository.findById(node.getParentId())
                    .ifPresent(parent -> dto.setParentName(parent.getName()));
        }
        
        // 统计子节点数量
        dto.setChildrenCount(organizationNodeRepository.countChildrenByParentId(node.getId()));
        
        // TODO: 统计数据文件数量
        dto.setDataFilesCount(0L);
        
        return dto;
    }

    /**
     * 获取当前用户名
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
