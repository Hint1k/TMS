package com.demo.tms.utils;

/**
 * {@code TaskPriority} is an enum that defines the priority levels for tasks.
 * <p>
 * This enum categorizes tasks into three priority levels: HIGH, MEDIUM, and LOW.
 * These levels help in determining the urgency or importance of a task in the system.
 * </p>
 */
public enum TaskPriority {
    /**
     * High priority tasks that need immediate attention and action.
     */
    HIGH,

    /**
     * Medium priority tasks that are important but not as urgent as high priority tasks.
     */
    MEDIUM,

    /**
     * Low priority tasks that can be addressed later.
     */
    LOW
}