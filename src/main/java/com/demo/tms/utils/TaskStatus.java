package com.demo.tms.utils;

/**
 * {@code TaskStatus} is an enum that represents the different statuses of a task.
 * <p>
 * This enum categorizes tasks into three possible states: PENDING, PROCESSING, and COMPLETED.
 * These statuses help in tracking the progress of a task throughout its lifecycle.
 * </p>
 */
public enum TaskStatus {
    /**
     * Indicates that the task has been created but is not yet started.
     */
    PENDING,

    /**
     * Indicates that the task is currently being worked on.
     */
    PROCESSING,

    /**
     * Indicates that the task has been completed successfully.
     */
    COMPLETED
}