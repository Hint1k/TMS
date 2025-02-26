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

/**
 * {@code GlobalExceptionHandler} is a centralized exception handler that intercepts various types of exceptions
 * thrown during the execution of the application and returns appropriate HTTP responses with problem details.
 * <p>
 * This class uses {@code @RestControllerAdvice} to handle exceptions globally in the application and returns
 * structured error responses to the client. Each exception handler is annotated with {@code @ExceptionHandler}
 * to catch specific exceptions and respond with relevant status codes and messages.
 * </p>
 * <p>
 * The exception handler methods log the exception details and create a standardized response containing:
 * - The error type
 * - The HTTP status code
 * - A human-readable title
 * - A detailed message
 * - The request URI that caused the error
 * </p>
 * <p>
 * Handled exceptions include:
 * - {@code ResourceNotFoundException}: Triggered when a requested resource is not found.
 * - {@code ObjectOptimisticLockingFailureException}: Triggered in case of a conflict due to concurrent modifications.
 * - {@code MethodArgumentNotValidException}: Triggered for validation errors during request processing.
 * - {@code IllegalArgumentException}: Triggered for invalid arguments in the request.
 * - {@code AccessDeniedException}: Triggered when a user does not have the necessary permissions.
 * - {@code DataIntegrityViolationException}: Triggered when a database constraint is violated.
 * - {@code MethodArgumentTypeMismatchException}: Triggered when there is a type mismatch in request parameters.
 * - {@code Exception}: A fallback handler for all other unexpected errors.
 * </p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles the {@link ResourceNotFoundException} and returns a {@link ProblemDetailResponse} with a 404 status.
     *
     * @param ex The exception object.
     * @param request The HTTP request that caused the exception.
     * @return A {@link ResponseEntity} containing the problem details.
     */
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

    /**
     * Handles the {@link ObjectOptimisticLockingFailureException} and returns a {@link ProblemDetailResponse}
     * with a 409 status.
     *
     * @param ex The exception object.
     * @param request The HTTP request that caused the exception.
     * @return A {@link ResponseEntity} containing the problem details.
     */
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

    /**
     * Handles the {@link MethodArgumentNotValidException} and returns a {@link ProblemDetailResponse}
     * with a 400 status. This handles validation errors that occur when the request parameters do not pass validation.
     *
     * @param ex The exception object.
     * @param request The HTTP request that caused the exception.
     * @return A {@link ResponseEntity} containing the problem details.
     */
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

    /**
     * Handles the {@link IllegalArgumentException} and returns a {@link ProblemDetailResponse} with a 400 status.
     *
     * @param ex The exception object.
     * @param request The HTTP request that caused the exception.
     * @return A {@link ResponseEntity} containing the problem details.
     */
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

    /**
     * Handles the {@link AccessDeniedException} and returns a {@link ProblemDetailResponse} with a 403 status.
     *
     * @param ex The exception object.
     * @param request The HTTP request that caused the exception.
     * @return A {@link ResponseEntity} containing the problem details.
     */
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

    /**
     * Handles the {@link DataIntegrityViolationException} and returns a {@link ProblemDetailResponse}
     * with a 409 status.
     *
     * @param ex The exception object.
     * @param request The HTTP request that caused the exception.
     * @return A {@link ResponseEntity} containing the problem details.
     */
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

    /**
     * Handles the {@link MethodArgumentTypeMismatchException} and returns a {@link ProblemDetailResponse}
     * with a 400 status.
     *
     * @param ex The exception object.
     * @param request The HTTP request that caused the exception.
     * @return A {@link ResponseEntity} containing the problem details.
     */
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

    /**
     * Handles all other unexpected exceptions and returns a {@link ProblemDetailResponse} with a 500 status.
     *
     * @param ex The exception object.
     * @param request The HTTP request that caused the exception.
     * @return A {@link ResponseEntity} containing the problem details.
     */
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

    /**
     * Helper method to create a standardized {@link ProblemDetailResponse} for error responses.
     *
     * @param type The error type.
     * @param status The HTTP status code.
     * @param title The error title.
     * @param detail The error detail message.
     * @param instance The request URI.
     * @return A {@link ProblemDetailResponse} object containing the error details.
     */
    private ProblemDetailResponse createProblemDetailResponse(String type, HttpStatus status, String title,
                                                              String detail, String instance) {
        return new ProblemDetailResponse(type, status.value(), title, detail, instance);
    }
}