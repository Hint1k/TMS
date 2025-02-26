package com.demo.tms.controller;

import com.demo.tms.converter.Converter;
import com.demo.tms.dto.PagedResponseDTO;
import com.demo.tms.dto.TaskDTO;
import com.demo.tms.entity.Task;
import com.demo.tms.exception.ResourceNotFoundException;
import com.demo.tms.service.TaskService;
import com.demo.tms.utils.TaskStatus;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * The {@code TaskController} class handles HTTP requests related to tasks.
 * It provides RESTful endpoints for creating, updating, deleting, and retrieving tasks.
 * The class utilizes the {@link TaskService} to perform operations on tasks and
 * {@link Converter} to convert between DTOs and entity objects.
 */
@RestController
@RequestMapping("/api/tasks")
@Slf4j
public class TaskController {

    private final TaskService taskService;
    private final Converter converter;

    /**
     * Constructs a new {@code TaskController} with the specified dependencies.
     *
     * @param taskService The service responsible for managing task data.
     * @param converter   The converter used to transform between {@link TaskDTO} and {@link Task} entities.
     */
    @Autowired
    public TaskController(TaskService taskService, Converter converter) {
        this.taskService = taskService;
        this.converter = converter;
    }

    /**
     * Creates a new task.
     * <p>
     * The method accepts a {@link TaskDTO} object, converts it to a {@link Task} entity,
     * saves it through the {@code taskService}, and returns the saved task as a {@link TaskDTO}.
     * </p>
     *
     * @param taskDTO The {@link TaskDTO} object containing the task data to be saved.
     * @return A {@link ResponseEntity} containing the saved task as a {@link TaskDTO}.
     */
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskDTO taskDTO) {
        Task task = converter.convertToTask(taskDTO);
        Task savedTask = taskService.saveTask(task);
        return ResponseEntity.ok(converter.convertToTaskDTO(savedTask));
    }

    /**
     * Updates an existing task.
     * <p>
     * The method accepts a {@link TaskDTO} object with updated data, converts it to a {@link Task} entity,
     * and updates the task with the given {@code taskId}. If the task is not found,
     * a {@link ResourceNotFoundException} is thrown.
     * </p>
     *
     * @param taskId   The ID of the task to be updated.
     * @param taskDTO  The {@link TaskDTO} object containing the updated task data.
     * @return A {@link ResponseEntity} containing the updated task as a {@link TaskDTO}.
     * @throws ResourceNotFoundException If the task with the given {@code taskId} is not found.
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDTO> updateTask(@Valid @PathVariable Long taskId, @RequestBody TaskDTO taskDTO) {
        Task updatedTask = converter.convertToTask(taskDTO);
        updatedTask.setTaskId(taskId);
        Task task = taskService.updateTask(taskId, updatedTask);
        if (task == null) {
            throw new ResourceNotFoundException("Task with ID " + taskId + " not found");
        }
        return ResponseEntity.ok(converter.convertToTaskDTO(task));
    }

    /**
     * Updates the status of an existing task.
     * <p>
     * The method accepts a task ID and a map containing the new status. If the status is valid,
     * it updates the task's status
     * through the {@code taskService}. If the status is invalid or missing,
     * it throws an {@link IllegalArgumentException}.
     * </p>
     *
     * @param taskId      The ID of the task to update.
     * @param statusUpdate A map containing the new status.
     * @return A {@link ResponseEntity} containing the updated task as a {@link TaskDTO}.
     * @throws IllegalArgumentException If the status field is empty or invalid.
     */
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskDTO> updateTaskStatus(@PathVariable Long taskId,
                                                    @RequestBody Map<String, String> statusUpdate) {
        // Extract new status
        String newStatus = statusUpdate.get("status");
        if (newStatus.isEmpty()) {
            throw new IllegalArgumentException("Status field is required.");
        }

        TaskStatus statusEnum;
        try {
            statusEnum = TaskStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }

        // Update task status
        Task task = taskService.updateTaskStatus(taskId, statusEnum);
        return ResponseEntity.ok(converter.convertToTaskDTO(task));
    }

    /**
     * Deletes a task by its ID.
     * <p>
     * The method deletes the task associated with the provided {@code taskId}.
     * If the task is not found, a {@link ResourceNotFoundException} is thrown.
     * </p>
     *
     * @param taskId The ID of the task to be deleted.
     * @return A {@link ResponseEntity} indicating the result of the delete operation.
     * @throws ResourceNotFoundException If the task with the given {@code taskId} is not found.
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        boolean isDeleted = taskService.deleteTask(taskId);
        if (!isDeleted) {
            throw new ResourceNotFoundException("Task with ID " + taskId + " not found");
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves a task by its ID.
     * <p>
     * The method fetches the task with the given {@code taskId}. If the task is found,
     * it returns the task as a {@link TaskDTO}. If not, it returns a {@code 404 Not Found} response.
     * </p>
     *
     * @param taskId The ID of the task to be retrieved.
     * @return A {@link ResponseEntity} containing the task as a {@link TaskDTO} or a {@code 404 Not Found} response.
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long taskId) {
        Task task = taskService.getTaskById(taskId);
        return task != null ? ResponseEntity.ok(converter.convertToTaskDTO(task)) : ResponseEntity.notFound().build();
    }

    /**
     * Retrieves all tasks with pagination.
     * <p>
     * The method returns a paginated list of all tasks, with each task represented as a {@link TaskDTO}.
     * </p>
     *
     * @param pageable Pageable object for pagination.
     * @return A {@link ResponseEntity} containing the paginated tasks as a {@link PagedResponseDTO} of {@link TaskDTO}.
     */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<TaskDTO>> getAllTasks(Pageable pageable) {
        Page<Task> tasks = taskService.getAllTasks(pageable);
        List<TaskDTO> taskDTOs = tasks.getContent().stream()
                .map(converter::convertToTaskDTO)
                .toList();

        PagedResponseDTO<TaskDTO> response = createResponse(taskDTOs, tasks);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves tasks assigned to a specific author with pagination.
     * <p>
     * The method returns a paginated list of tasks authored by the given {@code authorId},
     * with each task represented as a {@link TaskDTO}.
     * </p>
     *
     * @param authorId The ID of the author whose tasks are to be retrieved.
     * @param pageable Pageable object for pagination.
     * @return A {@link ResponseEntity} containing the paginated tasks as a {@link PagedResponseDTO} of {@link TaskDTO}.
     */
    @GetMapping("/author/{authorId}")
    public ResponseEntity<PagedResponseDTO<TaskDTO>> getTasksByAuthor(@PathVariable Long authorId, Pageable pageable) {
        Page<Task> tasks = taskService.getTasksByAuthor(authorId, pageable);
        List<TaskDTO> taskDTOs = tasks.getContent().stream()
                .map(converter::convertToTaskDTO)
                .toList();

        PagedResponseDTO<TaskDTO> response = createResponse(taskDTOs, tasks);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves tasks assigned to a specific assignee with pagination.
     * <p>
     * The method returns a paginated list of tasks assigned to the given {@code assigneeId},
     * with each task represented as a {@link TaskDTO}.
     * </p>
     *
     * @param assigneeId The ID of the assignee whose tasks are to be retrieved.
     * @param pageable Pageable object for pagination.
     * @return A {@link ResponseEntity} containing the paginated tasks as a {@link PagedResponseDTO} of {@link TaskDTO}.
     */
    @GetMapping("/assignee/{assigneeId}")
    public ResponseEntity<PagedResponseDTO<TaskDTO>> getTasksByAssignee(@PathVariable Long assigneeId,
                                                                        Pageable pageable) {
        Page<Task> tasks = taskService.getTasksByAssignee(assigneeId, pageable);
        List<TaskDTO> taskDTOs = tasks.getContent().stream()
                .map(converter::convertToTaskDTO)
                .toList();

        PagedResponseDTO<TaskDTO> response = createResponse(taskDTOs, tasks);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a paginated response for tasks.
     * <p>
     * The method converts the tasks into a {@link PagedResponseDTO} containing task DTOs,
     * along with pagination details.
     * </p>
     *
     * @param taskDTOs A list of {@link TaskDTO} objects to include in the response.
     * @param tasks The paginated list of tasks.
     * @return A {@link PagedResponseDTO} containing the task DTOs and pagination details.
     */
    private PagedResponseDTO<TaskDTO> createResponse(List<TaskDTO> taskDTOs, Page<Task> tasks) {
        return new PagedResponseDTO<>(
                taskDTOs,
                tasks.getNumber(),
                tasks.getSize(),
                tasks.getTotalElements(),
                tasks.getTotalPages()
        );
    }
}