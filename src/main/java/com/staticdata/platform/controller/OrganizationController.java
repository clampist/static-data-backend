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
 * Organization Management Controller Handles organization-related HTTP requests
 */
@RestController
@RequestMapping("/organization")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Organization Management",
        description = "Organization structure management related APIs")
public class OrganizationController {

    private final OrganizationService organizationService;

    /**
     * Get complete organization tree
     */
    @GetMapping("/tree")
    @Operation(summary = "Get organization tree",
            description = "Get complete organization structure tree")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = OrganizationNodeDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")})
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrganizationNodeDto>> getOrganizationTree() {
        log.info("Getting organization tree");

        List<OrganizationNodeDto> tree = organizationService.getOrganizationTree();
        return ResponseEntity.ok(tree);
    }

    /**
     * Get child nodes by parent node ID
     */
    @GetMapping("/nodes")
    @Operation(summary = "Get child nodes",
            description = "Get direct child nodes list by parent node ID")
    @ApiResponses(
            value = {@ApiResponse(responseCode = "200", description = "Retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthenticated"),
                    @ApiResponse(responseCode = "403", description = "Insufficient permissions")})
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrganizationNodeDto>> getChildrenByParentId(
            @Parameter(description = "Parent Node ID, get root nodes when empty") @RequestParam(
                    required = false) Long parentId) {

        log.info("Getting children for parent node: {}", parentId);

        List<OrganizationNodeDto> children = organizationService.getChildrenByParentId(parentId);
        return ResponseEntity.ok(children);
    }

    /**
     * Get node details by ID
     */
    @GetMapping("/nodes/{id}")
    @Operation(summary = "Get node details", description = "Get detailed information by node ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = OrganizationNodeDto.class))),
            @ApiResponse(responseCode = "404", description = "Node does not exist"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")})
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrganizationNodeDto> getNodeById(
            @Parameter(description = "Node ID", required = true) @PathVariable Long id) {

        log.info("Getting organization node by id: {}", id);

        OrganizationNodeDto node = organizationService.getNodeById(id);
        return ResponseEntity.ok(node);
    }

    /**
     * Create organization node
     */
    @PostMapping("/nodes")
    @Operation(summary = "Create organization node", description = "Create new organization node")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created successfully",
                    content = @Content(
                            schema = @Schema(implementation = OrganizationNodeDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "409", description = "Node name conflict"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")})
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrganizationNodeDto> createNode(
            @Parameter(description = "Create node request information",
                    required = true) @Valid @RequestBody CreateOrganizationNodeRequest request) {

        log.info("Creating organization node: {}", request.getName());

        OrganizationNodeDto createdNode = organizationService.createNode(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNode);
    }

    /**
     * Update organization node
     */
    @PutMapping("/nodes/{id}")
    @Operation(summary = "Update organization node",
            description = "Update specified node information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully",
                    content = @Content(
                            schema = @Schema(implementation = OrganizationNodeDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Node does not exist"),
            @ApiResponse(responseCode = "409", description = "Node name conflict"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")})
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrganizationNodeDto> updateNode(
            @Parameter(description = "Node ID", required = true) @PathVariable Long id,
            @Parameter(description = "Update node request information",
                    required = true) @Valid @RequestBody UpdateOrganizationNodeRequest request) {

        log.info("Updating organization node: {}", id);

        OrganizationNodeDto updatedNode = organizationService.updateNode(id, request);
        return ResponseEntity.ok(updatedNode);
    }

    /**
     * Delete organization node
     */
    @DeleteMapping("/nodes/{id}")
    @Operation(summary = "Delete organization node",
            description = "Delete specified organization node")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Deleted successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Cannot delete (contains child nodes or associated data)"),
            @ApiResponse(responseCode = "404", description = "Node does not exist"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")})
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteNode(
            @Parameter(description = "Node ID", required = true) @PathVariable Long id) {

        log.info("Deleting organization node: {}", id);

        organizationService.deleteNode(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search organization nodes
     */
    @GetMapping("/search")
    @Operation(summary = "Search organization nodes",
            description = "Search organization nodes by keywords")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Search successful"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")})
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrganizationNodeDto>> searchNodes(
            @Parameter(description = "Search keywords") @RequestParam String keyword) {

        log.info("Searching organization nodes with keyword: {}", keyword);

        List<OrganizationNodeDto> results = organizationService.searchNodes(keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * Move node
     */
    @PutMapping("/nodes/{id}/move")
    @Operation(summary = "Move node", description = "Move node to new parent node")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Moved successfully",
                    content = @Content(
                            schema = @Schema(implementation = OrganizationNodeDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Move failed (circular reference or name conflict)"),
            @ApiResponse(responseCode = "404", description = "Node does not exist"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")})
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrganizationNodeDto> moveNode(
            @Parameter(description = "Node ID", required = true) @PathVariable Long id, @Parameter(
                    description = "New parent node ID, null means move to root node") @RequestBody Map<String, Long> request) {

        Long newParentId = request.get("parentId");
        log.info("Moving node {} to parent {}", id, newParentId);

        OrganizationNodeDto movedNode = organizationService.moveNode(id, newParentId);
        return ResponseEntity.ok(movedNode);
    }

    /**
     * Get node type list
     */
    @GetMapping("/node-types")
    @Operation(summary = "Get node types", description = "Get all available node types")
    @ApiResponses(
            value = {@ApiResponse(responseCode = "200", description = "Retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthenticated"),
                    @ApiResponse(responseCode = "403", description = "Insufficient permissions")})
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrganizationNode.NodeType[]> getNodeTypes() {
        log.info("Getting node types");

        OrganizationNode.NodeType[] types = OrganizationNode.NodeType.values();
        return ResponseEntity.ok(types);
    }

    /**
     * Get node statistics
     */
    @GetMapping("/nodes/{id}/stats")
    @Operation(summary = "Get node statistics",
            description = "Get statistics information of specified node")
    @ApiResponses(
            value = {@ApiResponse(responseCode = "200", description = "Retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Node does not exist"),
                    @ApiResponse(responseCode = "401", description = "Unauthenticated"),
                    @ApiResponse(responseCode = "403", description = "Insufficient permissions")})
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getNodeStats(
            @Parameter(description = "Node ID", required = true) @PathVariable Long id) {

        log.info("Getting stats for node: {}", id);

        OrganizationNodeDto node = organizationService.getNodeById(id);

        Map<String, Object> stats = Map.of("id", node.getId(), "name", node.getName(),
                "childrenCount", node.getChildrenCount(), "dataFilesCount",
                node.getDataFilesCount(), "type", node.getType(), "createdAt", node.getCreatedAt(),
                "updatedAt", node.getUpdatedAt());

        return ResponseEntity.ok(stats);
    }
}
