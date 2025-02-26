package com.demo.testclient.dto;

import com.demo.testclient.enums.TaskPriority;
import com.demo.testclient.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 45, message = "Status must not exceed 45 characters")
    private TaskStatus status;

    @NotNull(message = "Priority is required")
    @Size(max = 45, message = "Priority must not exceed 45 characters")
    private TaskPriority priority;

    @NotNull(message = "Author ID is required")
    private Long authorId;

    @NotNull(message = "Assignee ID is required")
    private Long assigneeId;

    private List<Long> commentIds = new ArrayList<>();

    // no taskId
    public TaskDTO(String name, String description, TaskStatus status, TaskPriority priority, Long authorId,
                   Long assigneeId, List<Long> commentIds) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.authorId = authorId;
        this.assigneeId = assigneeId;
        this.commentIds = commentIds;
    }
}