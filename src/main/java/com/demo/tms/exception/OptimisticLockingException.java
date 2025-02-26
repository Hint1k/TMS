package com.demo.tms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * {@code OptimisticLockingException} is a custom exception thrown when an optimistic locking failure occurs.
 * <p>
 * Optimistic locking is used to prevent concurrent modification issues in an application where multiple
 * users might be trying to modify the same data. This exception is thrown when a conflict is detected during
 * the saving of data, indicating that the data has been modified by another user or process since it was
 * last read.
 * </p>
 * <p>
 * The exception is annotated with {@code @ResponseStatus(HttpStatus.CONFLICT)} to automatically return a
 * {@code 409 Conflict} HTTP status when the exception is thrown in a REST API context.
 * </p>
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class OptimisticLockingException extends RuntimeException {

    /**
     * Constructs a new {@code OptimisticLockingException} with the specified detail message.
     *
     * @param message The detail message explaining the cause of the exception.
     */
    public OptimisticLockingException(String message) {
        super(message);
    }
}