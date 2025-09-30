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
@Tag(name = "Data File Management", description = "Data file CRUD operations and query APIs")
public class DataFileController {

        private final DataFileService dataFileService;

        @PostMapping
        @Operation(summary = "Create data file",
                        description = "Create a new data file, must be attached to a functional module")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201",
                                        description = "Data file created successfully",
                                        content = @Content(schema = @Schema(
                                                        implementation = DataFileDto.class))),
                        @ApiResponse(responseCode = "400",
                                        description = "Invalid request parameters or business logic error"),
                        @ApiResponse(responseCode = "404",
                                        description = "Organization node does not exist")})
        public ResponseEntity<DataFileDto> createDataFile(@Parameter(
                        description = "Create data file request body",
                        required = true) @Valid @RequestBody CreateDataFileRequest request) {
                log.info("Received request to create data file: {}", request.getName());
                DataFileDto newDataFile = dataFileService.createDataFile(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(newDataFile);
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update data file",
                        description = "Update data file information by specified ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200",
                                        description = "Data file updated successfully",
                                        content = @Content(schema = @Schema(
                                                        implementation = DataFileDto.class))),
                        @ApiResponse(responseCode = "400",
                                        description = "Invalid request parameters or business logic error"),
                        @ApiResponse(responseCode = "404",
                                        description = "Data file does not exist")})
        public ResponseEntity<DataFileDto> updateDataFile(
                        @Parameter(description = "Data file ID",
                                        required = true) @PathVariable Long id,
                        @Parameter(description = "Update data file request body",
                                        required = true) @Valid @RequestBody UpdateDataFileRequest request) {
                log.info("Received request to update data file with ID: {}", id);
                DataFileDto updatedDataFile = dataFileService.updateDataFile(id, request);
                return ResponseEntity.ok(updatedDataFile);
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete data file", description = "Delete data file by specified ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204",
                                        description = "Data file deleted successfully"),
                        @ApiResponse(responseCode = "400", description = "Business logic error"),
                        @ApiResponse(responseCode = "404",
                                        description = "Data file does not exist")})
        public ResponseEntity<Void> deleteDataFile(@Parameter(description = "Data file ID",
                        required = true) @PathVariable Long id) {
                log.info("Received request to delete data file with ID: {}", id);
                dataFileService.deleteDataFile(id);
                return ResponseEntity.noContent().build();
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get data file details",
                        description = "Get detailed information of a single data file by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200",
                                        description = "Successfully retrieved data file details",
                                        content = @Content(schema = @Schema(
                                                        implementation = DataFileDto.class))),
                        @ApiResponse(responseCode = "404",
                                        description = "Data file does not exist"),
                        @ApiResponse(responseCode = "403",
                                        description = "No permission to access this data file")})
        public ResponseEntity<DataFileDto> getDataFileById(@Parameter(description = "Data file ID",
                        required = true) @PathVariable Long id) {
                log.info("Received request to get data file by ID: {}", id);
                DataFileDto dataFile = dataFileService.getDataFileById(id);
                return ResponseEntity.ok(dataFile);
        }

        @PostMapping("/query")
        @Operation(summary = "Query data files",
                        description = "Query data files with pagination based on conditions")
        @ApiResponses(value = {@ApiResponse(responseCode = "200",
                        description = "Successfully retrieved query results",
                        content = @Content(schema = @Schema(implementation = Page.class)))})
        public ResponseEntity<Page<DataFileDto>> queryDataFiles(@Parameter(
                        description = "Query conditions",
                        required = true) @Valid @RequestBody DataFileQueryRequest request) {
                log.info("Received request to query data files with conditions: {}", request);
                Page<DataFileDto> dataFiles = dataFileService.queryDataFiles(request);
                return ResponseEntity.ok(dataFiles);
        }

        @GetMapping("/organization/{organizationNodeId}")
        @Operation(summary = "Get data files under organization node",
                        description = "Get all data files under organization node by organization node ID")
        @ApiResponses(value = {@ApiResponse(responseCode = "200",
                        description = "Successfully retrieved data file list",
                        content = @Content(schema = @Schema(implementation = DataFileDto.class)))})
        public ResponseEntity<List<DataFileDto>> getDataFilesByOrganizationNode(
                        @Parameter(description = "Organization node ID",
                                        required = true) @PathVariable Long organizationNodeId) {
                log.info("Received request to get data files for organization node ID: {}",
                                organizationNodeId);
                List<DataFileDto> dataFiles =
                                dataFileService.getDataFilesByOrganizationNode(organizationNodeId);
                return ResponseEntity.ok(dataFiles);
        }

        @GetMapping("/owner/{ownerId}")
        @Operation(summary = "Get data files owned by user",
                        description = "Get all data files owned by user based on user ID")
        @ApiResponses(value = {@ApiResponse(responseCode = "200",
                        description = "Successfully retrieved data file list",
                        content = @Content(schema = @Schema(implementation = DataFileDto.class)))})
        public ResponseEntity<List<DataFileDto>> getDataFilesByOwner(@Parameter(
                        description = "User ID", required = true) @PathVariable Long ownerId) {
                log.info("Received request to get data files for owner ID: {}", ownerId);
                List<DataFileDto> dataFiles = dataFileService.getDataFilesByOwner(ownerId);
                return ResponseEntity.ok(dataFiles);
        }

        @GetMapping("/search")
        @Operation(summary = "Search data files", description = "Search data files by keywords")
        @ApiResponses(value = {@ApiResponse(responseCode = "200",
                        description = "Successfully retrieved search results",
                        content = @Content(schema = @Schema(implementation = DataFileDto.class)))})
        public ResponseEntity<List<DataFileDto>> searchDataFiles(
                        @Parameter(description = "Search keywords",
                                        required = true) @RequestParam String keyword) {
                log.info("Received request to search data files with keyword: {}", keyword);
                List<DataFileDto> dataFiles = dataFileService.searchDataFiles(keyword);
                return ResponseEntity.ok(dataFiles);
        }

        @GetMapping("/data-type/{dataType}")
        @Operation(summary = "Query data files by data type",
                        description = "Query data files containing columns of specified data type")
        @ApiResponses(value = {@ApiResponse(responseCode = "200",
                        description = "Successfully retrieved data file list",
                        content = @Content(schema = @Schema(implementation = DataFileDto.class)))})
        public ResponseEntity<List<DataFileDto>> getDataFilesByDataType(@Parameter(
                        description = "Data type",
                        required = true) @PathVariable DataFile.ColumnDefinition.DataType dataType) {
                log.info("Received request to get data files by data type: {}", dataType);
                List<DataFileDto> dataFiles = dataFileService.getDataFilesByDataType(dataType);
                return ResponseEntity.ok(dataFiles);
        }

        @GetMapping("/recent")
        @Operation(summary = "Get recently created data files",
                        description = "Get list of recently created data files")
        @ApiResponses(value = {@ApiResponse(responseCode = "200",
                        description = "Successfully retrieved recent data file list",
                        content = @Content(schema = @Schema(implementation = DataFileDto.class)))})
        public ResponseEntity<List<DataFileDto>> getRecentDataFiles(@Parameter(
                        description = "Return quantity limit",
                        required = false) @RequestParam(defaultValue = "10") int limit) {
                log.info("Received request to get recent data files with limit: {}", limit);
                List<DataFileDto> dataFiles = dataFileService.getRecentDataFiles(limit);
                return ResponseEntity.ok(dataFiles);
        }

        @GetMapping("/statistics")
        @Operation(summary = "Get data file statistics",
                        description = "Get statistics information of data files")
        @ApiResponses(value = {@ApiResponse(responseCode = "200",
                        description = "Successfully retrieved statistics",
                        content = @Content(schema = @Schema(implementation = Map.class)))})
        public ResponseEntity<Map<String, Object>> getDataFileStatistics() {
                log.info("Received request to get data file statistics");
                Map<String, Object> statistics = dataFileService.getDataFileStatistics();
                return ResponseEntity.ok(statistics);
        }

        @GetMapping("/accessible")
        @Operation(summary = "Get accessible data files for user",
                        description = "Get all data files accessible by current user (public and owned)")
        @ApiResponses(value = {@ApiResponse(responseCode = "200",
                        description = "Successfully retrieved accessible data file list",
                        content = @Content(schema = @Schema(implementation = DataFileDto.class)))})
        public ResponseEntity<List<DataFileDto>> getAccessibleDataFiles(
                        @Parameter(description = "Page number", required = false) @RequestParam(
                                        defaultValue = "0") int page,
                        @Parameter(description = "Page size", required = false) @RequestParam(
                                        defaultValue = "10") int size) {
                log.info("Received request to get accessible data files for current user");
                // Pagination logic needs to be implemented here, temporarily return all accessible
                // files
                List<DataFileDto> dataFiles = dataFileService.getRecentDataFiles(size * (page + 1));
                return ResponseEntity.ok(dataFiles);
        }

        @GetMapping("/data-types")
        @Operation(summary = "Get supported data types",
                        description = "Get all data types supported by the system")
        @ApiResponses(value = {@ApiResponse(responseCode = "200",
                        description = "Successfully retrieved data type list",
                        content = @Content(schema = @Schema(
                                        implementation = DataFile.ColumnDefinition.DataType.class)))})
        public ResponseEntity<List<DataFile.ColumnDefinition.DataType>> getSupportedDataTypes() {
                log.info("Received request to get supported data types");
                List<DataFile.ColumnDefinition.DataType> dataTypes =
                                List.of(DataFile.ColumnDefinition.DataType.STRING,
                                                DataFile.ColumnDefinition.DataType.INTEGER,
                                                DataFile.ColumnDefinition.DataType.DECIMAL,
                                                DataFile.ColumnDefinition.DataType.BOOLEAN,
                                                DataFile.ColumnDefinition.DataType.DATE,
                                                DataFile.ColumnDefinition.DataType.DATETIME,
                                                DataFile.ColumnDefinition.DataType.JSON);
                return ResponseEntity.ok(dataTypes);
        }
}
