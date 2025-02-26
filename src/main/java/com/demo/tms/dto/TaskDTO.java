package com.demo.tms.dto;

import com.demo.tms.utils.TaskPriority;
import com.demo.tms.utils.TaskStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code TaskDTO} is a Data Transfer Object (DTO) used to represent a task's information within the system.
 * It contains details such as the task's identifier, name, description, status, priority,
 * associated author and assignee, list of associated comment IDs, and version information.
 * This DTO is primarily used to transfer task-related data between different layers of the application.
 * <p>
 * This DTO includes validation constraints to ensure that the task information meets required criteria,
 * such as non-null fields, size limits, and proper enum values for status and priority.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO implements Serializable {

    /**
     * The unique identifier for the task.
     * This field is used for database lookups and identifying the task within the system.
     */
    private Long taskId;

    /**
     * The name of the task.
     * This field is required and must not exceed 255 characters.
     * It represents a brief description or title of the task.
     */
    @NotBlank(message = "Task name is required")
    @Size(max = 255, message = "Task name must not exceed 255 characters")
    private String name;

    /**
     * The detailed description of the task.
     * This field is required and provides more information about the task's purpose.
     */
    @NotBlank(message = "Task description is required")
    private String description;

    /**
     * The status of the task.
     * This field is required and represents the current state of the task (e.g., Pending, In Progress, Completed).
     * The status is an enum value from {@link TaskStatus}.
     */
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    /**
     * The priority of the task.
     * This field is required and represents the importance level of the task (e.g., Low, Medium, High).
     * The priority is an enum value from {@link TaskPriority}.
     */
    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    /**
     * The unique identifier of the user who created the task (author).
     * This field is required and links the task to the user who created it.
     */
    @NotNull(message = "Author ID is required")
    private Long authorId;

    /**
     * The unique identifier of the user assigned to the task (assignee).
     * This field is required and links the task to the user who is responsible for completing it.
     */
    @NotNull(message = "Assignee ID is required")
    private Long assigneeId;

    /**
     * A list of comment IDs associated with the task.
     * This field may be empty if there are no comments associated with the task.
     */
    private List<Long> commentIds = new ArrayList<>();

    /**
     * The version of the task.
     * This field is used for optimistic locking to handle concurrent updates to the task.
     */
    private Long version;

    /**
     * Constructor without taskId, used for cases where the taskId is not required (e.g., task creation).
     *
     * @param name The name of the task.
     * @param description The description of the task.
     * @param status The status of the task.
     * @param priority The priority of the task.
     * @param authorId The user ID of the task author.
     * @param assigneeId The user ID of the task assignee.
     * @param commentIds The list of comment IDs associated with the task.
     * @param version The version of the task.
     */
    public TaskDTO(String name, String description, TaskStatus status, TaskPriority priority, Long authorId,
                   Long assigneeId, List<Long> commentIds, Long version) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.authorId = authorId;
        this.assigneeId = assigneeId;
        this.commentIds = commentIds;
        this.version = version;
    }
}