package com.demo.tms.exception;

/**
 * {@code ResourceNotFoundException} is a custom exception thrown when a requested resource cannot be found.
 * <p>
 * This exception is typically thrown when a resource (e.g., a user, task, comment) that is expected to exist
 * in the system cannot be located, often based on its identifier. It helps to communicate that the requested
 * resource is missing or deleted.
 * </p>
 * <p>
 * The exception does not specify a particular HTTP status, but it can be mapped to an appropriate HTTP response
 * status (e.g., {@code 404 Not Found}) in the controller.
 * </p>
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code ResourceNotFoundException} with the specified detail message.
     *
     * @param message The detail message explaining the cause of the exception.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}