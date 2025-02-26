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

@RestController
@RequestMapping("/api/tasks")
@Slf4j
public class TaskController {

    private final TaskService taskService;
    private final Converter converter;

    @Autowired
    public TaskController(TaskService taskService, Converter converter) {
        this.taskService = taskService;
        this.converter = converter;
    }

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskDTO taskDTO) {
        Task task = converter.convertToTask(taskDTO);
        Task savedTask = taskService.saveTask(task);
        return ResponseEntity.ok(converter.convertToTaskDTO(savedTask));
    }

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

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        boolean isDeleted = taskService.deleteTask(taskId);
        if (!isDeleted) {
            throw new ResourceNotFoundException("Task with ID " + taskId + " not found");
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long taskId) {
        Task task = taskService.getTaskById(taskId);
        return task != null ? ResponseEntity.ok(converter.convertToTaskDTO(task)) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<PagedResponseDTO<TaskDTO>> getAllTasks(Pageable pageable) {
        Page<Task> tasks = taskService.getAllTasks(pageable);
        List<TaskDTO> taskDTOs = tasks.getContent().stream()
                .map(converter::convertToTaskDTO)
                .toList();

        PagedResponseDTO<TaskDTO> response = createResponse(taskDTOs, tasks);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<PagedResponseDTO<TaskDTO>> getTasksByAuthor(@PathVariable Long authorId, Pageable pageable) {
        Page<Task> tasks = taskService.getTasksByAuthor(authorId, pageable);
        List<TaskDTO> taskDTOs = tasks.getContent().stream()
                .map(converter::convertToTaskDTO)
                .toList();

        PagedResponseDTO<TaskDTO> response = createResponse(taskDTOs, tasks);
        return ResponseEntity.ok(response);
    }

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