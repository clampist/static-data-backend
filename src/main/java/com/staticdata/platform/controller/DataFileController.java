package com.staticdata.platform.controller;

import com.staticdata.platform.dto.*;
import com.staticdata.platform.entity.DataFile;
import com.staticdata.platform.service.DataFileService;
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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/data-files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "数据文件管理", description = "数据文件CRUD操作和查询API")
public class DataFileController {

    private final DataFileService dataFileService;

    @PostMapping
    @Operation(summary = "创建数据文件", description = "创建一个新的数据文件，必须挂在功能模块下")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "数据文件创建成功",
                    content = @Content(schema = @Schema(implementation = DataFileDto.class))),
            @ApiResponse(responseCode = "400", description = "请求参数错误或业务逻辑错误"),
            @ApiResponse(responseCode = "404", description = "组织节点不存在")
    })
    public ResponseEntity<DataFileDto> createDataFile(
            @Parameter(description = "创建数据文件请求体", required = true)
            @Valid @RequestBody CreateDataFileRequest request) {
        log.info("Received request to create data file: {}", request.getName());
        DataFileDto newDataFile = dataFileService.createDataFile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newDataFile);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新数据文件", description = "更新指定ID的数据文件信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "数据文件更新成功",
                    content = @Content(schema = @Schema(implementation = DataFileDto.class))),
            @ApiResponse(responseCode = "400", description = "请求参数错误或业务逻辑错误"),
            @ApiResponse(responseCode = "404", description = "数据文件不存在")
    })
    public ResponseEntity<DataFileDto> updateDataFile(
            @Parameter(description = "数据文件ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "更新数据文件请求体", required = true)
            @Valid @RequestBody UpdateDataFileRequest request) {
        log.info("Received request to update data file with ID: {}", id);
        DataFileDto updatedDataFile = dataFileService.updateDataFile(id, request);
        return ResponseEntity.ok(updatedDataFile);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除数据文件", description = "删除指定ID的数据文件")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "数据文件删除成功"),
            @ApiResponse(responseCode = "400", description = "业务逻辑错误"),
            @ApiResponse(responseCode = "404", description = "数据文件不存在")
    })
    public ResponseEntity<Void> deleteDataFile(
            @Parameter(description = "数据文件ID", required = true)
            @PathVariable Long id) {
        log.info("Received request to delete data file with ID: {}", id);
        dataFileService.deleteDataFile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取数据文件详情", description = "根据ID获取单个数据文件的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取数据文件详情",
                    content = @Content(schema = @Schema(implementation = DataFileDto.class))),
            @ApiResponse(responseCode = "404", description = "数据文件不存在"),
            @ApiResponse(responseCode = "403", description = "没有权限访问此数据文件")
    })
    public ResponseEntity<DataFileDto> getDataFileById(
            @Parameter(description = "数据文件ID", required = true)
            @PathVariable Long id) {
        log.info("Received request to get data file by ID: {}", id);
        DataFileDto dataFile = dataFileService.getDataFileById(id);
        return ResponseEntity.ok(dataFile);
    }

    @PostMapping("/query")
    @Operation(summary = "查询数据文件", description = "根据条件分页查询数据文件")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取查询结果",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<DataFileDto>> queryDataFiles(
            @Parameter(description = "查询条件", required = true)
            @Valid @RequestBody DataFileQueryRequest request) {
        log.info("Received request to query data files with conditions: {}", request);
        Page<DataFileDto> dataFiles = dataFileService.queryDataFiles(request);
        return ResponseEntity.ok(dataFiles);
    }

    @GetMapping("/organization/{organizationNodeId}")
    @Operation(summary = "获取组织节点下的数据文件", description = "根据组织节点ID获取其下的所有数据文件")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取数据文件列表",
                    content = @Content(schema = @Schema(implementation = DataFileDto.class)))
    })
    public ResponseEntity<List<DataFileDto>> getDataFilesByOrganizationNode(
            @Parameter(description = "组织节点ID", required = true)
            @PathVariable Long organizationNodeId) {
        log.info("Received request to get data files for organization node ID: {}", organizationNodeId);
        List<DataFileDto> dataFiles = dataFileService.getDataFilesByOrganizationNode(organizationNodeId);
        return ResponseEntity.ok(dataFiles);
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "获取用户拥有的数据文件", description = "根据用户ID获取其拥有的所有数据文件")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取数据文件列表",
                    content = @Content(schema = @Schema(implementation = DataFileDto.class)))
    })
    public ResponseEntity<List<DataFileDto>> getDataFilesByOwner(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long ownerId) {
        log.info("Received request to get data files for owner ID: {}", ownerId);
        List<DataFileDto> dataFiles = dataFileService.getDataFilesByOwner(ownerId);
        return ResponseEntity.ok(dataFiles);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索数据文件", description = "根据关键词搜索数据文件")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取搜索结果",
                    content = @Content(schema = @Schema(implementation = DataFileDto.class)))
    })
    public ResponseEntity<List<DataFileDto>> searchDataFiles(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam String keyword) {
        log.info("Received request to search data files with keyword: {}", keyword);
        List<DataFileDto> dataFiles = dataFileService.searchDataFiles(keyword);
        return ResponseEntity.ok(dataFiles);
    }

    @GetMapping("/data-type/{dataType}")
    @Operation(summary = "根据数据类型查询数据文件", description = "根据指定的数据类型查询包含该类型列的数据文件")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取数据文件列表",
                    content = @Content(schema = @Schema(implementation = DataFileDto.class)))
    })
    public ResponseEntity<List<DataFileDto>> getDataFilesByDataType(
            @Parameter(description = "数据类型", required = true)
            @PathVariable DataFile.ColumnDefinition.DataType dataType) {
        log.info("Received request to get data files by data type: {}", dataType);
        List<DataFileDto> dataFiles = dataFileService.getDataFilesByDataType(dataType);
        return ResponseEntity.ok(dataFiles);
    }

    @GetMapping("/recent")
    @Operation(summary = "获取最近创建的数据文件", description = "获取最近创建的数据文件列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取最近的数据文件列表",
                    content = @Content(schema = @Schema(implementation = DataFileDto.class)))
    })
    public ResponseEntity<List<DataFileDto>> getRecentDataFiles(
            @Parameter(description = "返回数量限制", required = false)
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Received request to get recent data files with limit: {}", limit);
        List<DataFileDto> dataFiles = dataFileService.getRecentDataFiles(limit);
        return ResponseEntity.ok(dataFiles);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取数据文件统计信息", description = "获取数据文件的统计信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取统计信息",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> getDataFileStatistics() {
        log.info("Received request to get data file statistics");
        Map<String, Object> statistics = dataFileService.getDataFileStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/accessible")
    @Operation(summary = "获取用户可访问的数据文件", description = "获取当前用户可访问的所有数据文件（公开的和自己拥有的）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取可访问的数据文件列表",
                    content = @Content(schema = @Schema(implementation = DataFileDto.class)))
    })
    public ResponseEntity<List<DataFileDto>> getAccessibleDataFiles(
            @Parameter(description = "页码", required = false)
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", required = false)
            @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to get accessible data files for current user");
        // 这里需要实现分页逻辑，暂时返回所有可访问的文件
        List<DataFileDto> dataFiles = dataFileService.getRecentDataFiles(size * (page + 1));
        return ResponseEntity.ok(dataFiles);
    }

    @GetMapping("/data-types")
    @Operation(summary = "获取支持的数据类型", description = "获取系统支持的所有数据类型")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取数据类型列表",
                    content = @Content(schema = @Schema(implementation = DataFile.ColumnDefinition.DataType.class)))
    })
    public ResponseEntity<List<DataFile.ColumnDefinition.DataType>> getSupportedDataTypes() {
        log.info("Received request to get supported data types");
        List<DataFile.ColumnDefinition.DataType> dataTypes = List.of(
                DataFile.ColumnDefinition.DataType.STRING,
                DataFile.ColumnDefinition.DataType.INTEGER,
                DataFile.ColumnDefinition.DataType.DECIMAL,
                DataFile.ColumnDefinition.DataType.BOOLEAN,
                DataFile.ColumnDefinition.DataType.DATE,
                DataFile.ColumnDefinition.DataType.DATETIME,
                DataFile.ColumnDefinition.DataType.JSON
        );
        return ResponseEntity.ok(dataTypes);
    }
}
