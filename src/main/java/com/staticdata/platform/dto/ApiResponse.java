package com.staticdata.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Generic API Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "API response")
public class ApiResponse<T> {

    @Schema(description = "Whether successful", example = "true")
    private Boolean success;

    @Schema(description = "Response message", example = "Operation successful")
    private String message;

    @Schema(description = "Response data")
    private T data;

    @Schema(description = "Error code")
    private String errorCode;

    @Schema(description = "Timestamp")
    private Long timestamp;

    public ApiResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public ApiResponse(Boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operation successful", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message);
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        ApiResponse<T> response = new ApiResponse<>(false, message);
        response.setErrorCode(errorCode);
        return response;
    }
}
