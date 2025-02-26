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

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO implements Serializable {

    private Long taskId;

    @NotBlank(message = "Task name is required")
    @Size(max = 255, message = "Task name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Task description is required")
    private String description;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    @NotNull(message = "Author ID is required")
    private Long authorId;

    @NotNull(message = "Assignee ID is required")
    private Long assigneeId;

    private List<Long> commentIds = new ArrayList<>();

    private Long version;

    // no taskId
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