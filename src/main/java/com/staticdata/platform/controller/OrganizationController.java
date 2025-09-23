package com.staticdata.platform.controller;

import com.staticdata.platform.dto.CreateOrganizationNodeRequest;
import com.staticdata.platform.dto.OrganizationNodeDto;
import com.staticdata.platform.dto.UpdateOrganizationNodeRequest;
import com.staticdata.platform.entity.OrganizationNode;
import com.staticdata.platform.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 组织管理控制器
 * 处理组织架构相关的HTTP请求
 */
@RestController
@RequestMapping("/organization")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "组织管理", description = "组织架构管理相关API")
public class OrganizationController {

    private final OrganizationService organizationService;

    /**
     * 获取完整的组织树
     */
    @GetMapping("/tree")
    @Operation(summary = "获取组织树", description = "获取完整的组织架构树状结构")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功", 
                    content = @Content(schema = @Schema(implementation = OrganizationNodeDto.class))),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrganizationNodeDto>> getOrganizationTree() {
        log.info("Getting organization tree");
        
        List<OrganizationNodeDto> tree = organizationService.getOrganizationTree();
        return ResponseEntity.ok(tree);
    }

    /**
     * 根据父节点ID获取子节点
     */
    @GetMapping("/nodes")
    @Operation(summary = "获取子节点", description = "根据父节点ID获取直接子节点列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrganizationNodeDto>> getChildrenByParentId(
            @Parameter(description = "父节点ID，为空时获取根节点")
            @RequestParam(required = false) Long parentId) {
        
        log.info("Getting children for parent node: {}", parentId);
        
        List<OrganizationNodeDto> children = organizationService.getChildrenByParentId(parentId);
        return ResponseEntity.ok(children);
    }

    /**
     * 根据ID获取节点详情
     */
    @GetMapping("/nodes/{id}")
    @Operation(summary = "获取节点详情", description = "根据节点ID获取详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功", 
                    content = @Content(schema = @Schema(implementation = OrganizationNodeDto.class))),
        @ApiResponse(responseCode = "404", description = "节点不存在"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrganizationNodeDto> getNodeById(
            @Parameter(description = "节点ID", required = true)
            @PathVariable Long id) {
        
        log.info("Getting organization node by id: {}", id);
        
        OrganizationNodeDto node = organizationService.getNodeById(id);
        return ResponseEntity.ok(node);
    }

    /**
     * 创建组织节点
     */
    @PostMapping("/nodes")
    @Operation(summary = "创建组织节点", description = "创建新的组织节点")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "创建成功", 
                    content = @Content(schema = @Schema(implementation = OrganizationNodeDto.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "409", description = "节点名称冲突"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrganizationNodeDto> createNode(
            @Parameter(description = "创建节点请求信息", required = true)
            @Valid @RequestBody CreateOrganizationNodeRequest request) {
        
        log.info("Creating organization node: {}", request.getName());
        
        OrganizationNodeDto createdNode = organizationService.createNode(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNode);
    }

    /**
     * 更新组织节点
     */
    @PutMapping("/nodes/{id}")
    @Operation(summary = "更新组织节点", description = "更新指定节点的信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功", 
                    content = @Content(schema = @Schema(implementation = OrganizationNodeDto.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "节点不存在"),
        @ApiResponse(responseCode = "409", description = "节点名称冲突"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrganizationNodeDto> updateNode(
            @Parameter(description = "节点ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "更新节点请求信息", required = true)
            @Valid @RequestBody UpdateOrganizationNodeRequest request) {
        
        log.info("Updating organization node: {}", id);
        
        OrganizationNodeDto updatedNode = organizationService.updateNode(id, request);
        return ResponseEntity.ok(updatedNode);
    }

    /**
     * 删除组织节点
     */
    @DeleteMapping("/nodes/{id}")
    @Operation(summary = "删除组织节点", description = "删除指定的组织节点")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "删除成功"),
        @ApiResponse(responseCode = "400", description = "无法删除（包含子节点或关联数据）"),
        @ApiResponse(responseCode = "404", description = "节点不存在"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteNode(
            @Parameter(description = "节点ID", required = true)
            @PathVariable Long id) {
        
        log.info("Deleting organization node: {}", id);
        
        organizationService.deleteNode(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 搜索组织节点
     */
    @GetMapping("/search")
    @Operation(summary = "搜索组织节点", description = "根据关键词搜索组织节点")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "搜索成功"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrganizationNodeDto>> searchNodes(
            @Parameter(description = "搜索关键词")
            @RequestParam String keyword) {
        
        log.info("Searching organization nodes with keyword: {}", keyword);
        
        List<OrganizationNodeDto> results = organizationService.searchNodes(keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * 移动节点
     */
    @PutMapping("/nodes/{id}/move")
    @Operation(summary = "移动节点", description = "将节点移动到新的父节点下")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "移动成功", 
                    content = @Content(schema = @Schema(implementation = OrganizationNodeDto.class))),
        @ApiResponse(responseCode = "400", description = "移动失败（循环引用或名称冲突）"),
        @ApiResponse(responseCode = "404", description = "节点不存在"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrganizationNodeDto> moveNode(
            @Parameter(description = "节点ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "新的父节点ID，null表示移动到根节点")
            @RequestBody Map<String, Long> request) {
        
        Long newParentId = request.get("parentId");
        log.info("Moving node {} to parent {}", id, newParentId);
        
        OrganizationNodeDto movedNode = organizationService.moveNode(id, newParentId);
        return ResponseEntity.ok(movedNode);
    }

    /**
     * 获取节点类型列表
     */
    @GetMapping("/node-types")
    @Operation(summary = "获取节点类型", description = "获取所有可用的节点类型")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrganizationNode.NodeType[]> getNodeTypes() {
        log.info("Getting node types");
        
        OrganizationNode.NodeType[] types = OrganizationNode.NodeType.values();
        return ResponseEntity.ok(types);
    }

    /**
     * 获取节点的统计信息
     */
    @GetMapping("/nodes/{id}/stats")
    @Operation(summary = "获取节点统计", description = "获取指定节点的统计信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "节点不存在"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getNodeStats(
            @Parameter(description = "节点ID", required = true)
            @PathVariable Long id) {
        
        log.info("Getting stats for node: {}", id);
        
        OrganizationNodeDto node = organizationService.getNodeById(id);
        
        Map<String, Object> stats = Map.of(
                "id", node.getId(),
                "name", node.getName(),
                "childrenCount", node.getChildrenCount(),
                "dataFilesCount", node.getDataFilesCount(),
                "type", node.getType(),
                "createdAt", node.getCreatedAt(),
                "updatedAt", node.getUpdatedAt()
        );
        
        return ResponseEntity.ok(stats);
    }
}
