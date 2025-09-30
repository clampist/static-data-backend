package com.staticdata.platform.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler uniformly handles various exceptions in the application and returns
 * standard ErrorResponse
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * HandleAuthenticationFailureException
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex,
            WebRequest request) {

        log.warn("Authentication failed: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value()).error("Authentication Failed")
                .message("Username or password error").path(request.getDescription(false)).build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * HandleUserDoes not existException
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
            UsernameNotFoundException ex, WebRequest request) {

        log.warn("User not found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value()).error("User Not Found")
                .message("UserDoes not exist").path(request.getDescription(false)).build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle user account disabled exception
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(DisabledException ex,
            WebRequest request) {

        log.warn("Account disabled: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value()).error("Account Disabled")
                .message("Account has been disabled").path(request.getDescription(false)).build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handle parameter validation exception
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        log.warn("Validation failed: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value()).error("Validation Failed")
                .message("Request parameter validation failed").path(request.getDescription(false))
                .details(errors).build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle illegal parameter exception
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex,
            WebRequest request) {

        log.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value()).error("Bad Request")
                .message(ex.getMessage()).path(request.getDescription(false)).build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle JWT related exception
     */
    @ExceptionHandler({io.jsonwebtoken.ExpiredJwtException.class,
            io.jsonwebtoken.UnsupportedJwtException.class,
            io.jsonwebtoken.MalformedJwtException.class,
            io.jsonwebtoken.security.SignatureException.class})
    public ResponseEntity<ErrorResponse> handleJwtException(Exception ex, WebRequest request) {

        log.warn("JWT error: {}", ex.getMessage());

        String message;
        if (ex instanceof io.jsonwebtoken.ExpiredJwtException) {
            message = "tokenExpired";
        } else if (ex instanceof io.jsonwebtoken.UnsupportedJwtException) {
            message = "Unsupported token format";
        } else if (ex instanceof io.jsonwebtoken.MalformedJwtException) {
            message = "tokenFormatError";
        } else if (ex instanceof io.jsonwebtoken.security.SignatureException) {
            message = "tokenSignatureValidateFailure";
        } else {
            message = "tokenInvalid";
        }

        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value()).error("JWT Error").message(message)
                .path(request.getDescription(false)).build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle access denied exception
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex, WebRequest request) {

        log.warn("Access denied: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value()).error("Access Denied")
                .message("No access permission").path(request.getDescription(false)).build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handle resource not found exception
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        log.warn("Resource not found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value()).error("Resource Not Found")
                .message(ex.getMessage()).path(request.getDescription(false)).build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle business exception
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex,
            WebRequest request) {

        log.warn("Business exception: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value()).error("Business Error")
                .message(ex.getMessage()).path(request.getDescription(false)).build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle runtime exception
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex,
            WebRequest request) {

        log.error("Runtime exception: {}", ex.getMessage(), ex);

        // Check if it is authentication-related exception
        if (ex.getMessage() != null && ex.getMessage().contains("Invalid credentials")) {
            ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                    .status(HttpStatus.UNAUTHORIZED.value()).error("Authentication Failed")
                    .message("Username or password error").path(request.getDescription(false))
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        // Other runtime exceptions
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error("Internal Server Error")
                .message("ServerInternalError").path(request.getDescription(false)).build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handle general exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {

        log.error("Unexpected exception: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error("Internal Server Error")
                .message("ServerInternalError").path(request.getDescription(false)).build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
