package com.demo.tms.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetailResponse> handleResourceNotFound(ResourceNotFoundException ex,
                                                                        HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        ProblemDetailResponse response = createProblemDetailResponse(
                "resource-not-found",
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetailResponse> handleOptimisticLockingFailure(
            ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        log.warn("Optimistic locking failure occurred: {}", ex.getMessage(), ex);
        ProblemDetailResponse response = createProblemDetailResponse(
                "optimistic-locking-failure",
                HttpStatus.CONFLICT,
                "Conflict",
                "The data has been modified by another user. Please refresh and try again.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetailResponse> handleValidationErrors(MethodArgumentNotValidException ex,
                                                                        HttpServletRequest request) {
        log.warn("Validation error occurred: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        String detail = errors.toString();
        ProblemDetailResponse response = createProblemDetailResponse(
                "validation-error",
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                detail,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetailResponse> handleIllegalArgumentException(IllegalArgumentException ex,
                                                                                HttpServletRequest request) {
        log.warn("Invalid argument provided: {}", ex.getMessage());
        ProblemDetailResponse response = createProblemDetailResponse(
                "general-error",
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Invalid argument: " + ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetailResponse> handleAccessDeniedException(AccessDeniedException ex,
                                                                             HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        ProblemDetailResponse response = createProblemDetailResponse(
                "access-denied",
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "Access denied.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetailResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Database constraint violation: {}", ex.getMessage());
        ProblemDetailResponse response = createProblemDetailResponse(
                "data-integrity-violation",
                HttpStatus.CONFLICT,
                "Conflict",
                "Database constraint violation.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetailResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                    HttpServletRequest request) {
        log.warn("Argument type mismatch: {}", ex.getMessage());
        String detail = "The parameter '" + ex.getName() + "' should be of type " +
                Objects.requireNonNull(ex.getRequiredType()).getName() +
                ". Provided value: " + ex.getValue();
        ProblemDetailResponse response = createProblemDetailResponse(
                "argument-type-mismatch",
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                detail,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetailResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        ProblemDetailResponse response = createProblemDetailResponse(
                "general-error",
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred: " + ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // Helper method to create ProblemDetailResponse
    private ProblemDetailResponse createProblemDetailResponse(String type, HttpStatus status, String title,
                                                              String detail, String instance) {
        return new ProblemDetailResponse(type, status.value(), title, detail, instance);
    }
}