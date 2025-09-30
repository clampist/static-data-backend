package com.staticdata.platform.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response class. Used to unify error response format in the application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Error occurrence time
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code
     */
    private Integer status;

    /**
     * Error type
     */
    private String error;

    /**
     * Error message
     */
    private String message;

    /**
     * RequestPath
     */
    private String path;

    /**
     * Detailed error information (such as field validation errors)
     */
    private Map<String, String> details;
}
