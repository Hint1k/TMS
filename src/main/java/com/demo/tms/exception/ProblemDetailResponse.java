package com.demo.tms.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * {@code ProblemDetailResponse} represents a standardized response structure for error details.
 * It is used to provide detailed information about errors encountered in the application, typically
 * in the context of HTTP error responses.
 * <p>
 * The class encapsulates key information about the error, including:
 * - {@code type}: A short, machine-readable identifier for the error type.
 * - {@code status}: The HTTP status code associated with the error.
 * - {@code title}: A human-readable title describing the error.
 * - {@code detail}: A detailed description of the error, providing additional context.
 * - {@code instance}: The request URI or other relevant information about the request that caused the error.
 * </p>
 * <p>
 * This class is typically used as the response body in REST API responses when handling exceptions,
 * ensuring a consistent format for error details across the application.
 * </p>
 */
@Data
@AllArgsConstructor
public class ProblemDetailResponse {
    /**
     * The type of the error, typically a machine-readable identifier.
     */
    private String type;

    /**
     * The HTTP status code associated with the error.
     */
    private int status;

    /**
     * A human-readable title describing the error.
     */
    private String title;

    /**
     * A detailed description of the error, providing additional context.
     */
    private String detail;

    /**
     * The URI or other relevant information about the request that caused the error.
     */
    private String instance;
}