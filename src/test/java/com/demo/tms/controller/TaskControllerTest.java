package com.demo.tms.controller;

import com.demo.tms.converter.Converter;
import com.demo.tms.dto.PagedResponseDTO;
import com.demo.tms.dto.TaskDTO;
import com.demo.tms.entity.Task;
import com.demo.tms.exception.ResourceNotFoundException;
import com.demo.tms.service.TaskService;
import com.demo.tms.utils.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @Mock
    private Converter converter;

    @InjectMocks
    private TaskController taskController;

    private Task task;
    private TaskDTO taskDTO;

    @BeforeEach
    void setUp() {
        task = new Task();
        task.setTaskId(1L);
        task.setName("Test Task");
        task.setDescription("Task description");
        task.setStatus(TaskStatus.PENDING);

        taskDTO = new TaskDTO();
        taskDTO.setTaskId(1L);
        taskDTO.setName("Test Task");
        taskDTO.setDescription("Task description");
        taskDTO.setStatus(TaskStatus.PENDING);
    }

    @Test
    void createTask_ShouldReturnTaskDTO() {
        when(converter.convertToTask(taskDTO)).thenReturn(task);
        when(taskService.saveTask(task)).thenReturn(task);
        when(converter.convertToTaskDTO(task)).thenReturn(taskDTO);

        ResponseEntity<TaskDTO> response = taskController.createTask(taskDTO);

        assertNotNull(response);
        assertEquals(taskDTO, response.getBody());
        verify(taskService, times(1)).saveTask(task);
    }

    @Test
    void updateTask_ShouldReturnUpdatedTaskDTO() {
        when(converter.convertToTask(taskDTO)).thenReturn(task);
        when(taskService.updateTask(1L, task)).thenReturn(task);
        when(converter.convertToTaskDTO(task)).thenReturn(taskDTO);

        ResponseEntity<TaskDTO> response = taskController.updateTask(1L, taskDTO);

        assertNotNull(response);
        assertEquals(taskDTO, response.getBody());
        verify(taskService, times(1)).updateTask(1L, task);
    }

    @Test
    void updateTask_ShouldThrowResourceNotFoundException_WhenTaskNotFound() {
        when(converter.convertToTask(taskDTO)).thenReturn(task);
        when(taskService.updateTask(1L, task)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> taskController.updateTask(1L, taskDTO));
    }

    @Test
    void updateTaskStatus_ShouldReturnUpdatedTaskDTO() {
        Map<String, String> statusUpdate = Map.of("status", "COMPLETED");
        when(taskService.updateTaskStatus(1L, TaskStatus.COMPLETED)).thenReturn(task);
        when(converter.convertToTaskDTO(task)).thenReturn(taskDTO);

        ResponseEntity<TaskDTO> response = taskController.updateTaskStatus(1L, statusUpdate);

        assertNotNull(response);
        assertEquals(taskDTO, response.getBody());
        verify(taskService, times(1)).updateTaskStatus(1L, TaskStatus.COMPLETED);
    }

    @Test
    void updateTaskStatus_ShouldThrowIllegalArgumentException_WhenStatusIsInvalid() {
        Map<String, String> statusUpdate = Map.of("status", "INVALID_STATUS");

        assertThrows(IllegalArgumentException.class, () -> taskController.updateTaskStatus(1L, statusUpdate));
    }

    @Test
    void deleteTask_ShouldReturnNoContent_WhenTaskDeleted() {
        when(taskService.deleteTask(1L)).thenReturn(true);

        ResponseEntity<Void> response = taskController.deleteTask(1L);

        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        verify(taskService, times(1)).deleteTask(1L);
    }

    @Test
    void deleteTask_ShouldThrowResourceNotFoundException_WhenTaskNotFound() {
        when(taskService.deleteTask(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> taskController.deleteTask(1L));
    }

    @Test
    void getTaskById_ShouldReturnTaskDTO_WhenTaskExists() {
        when(taskService.getTaskById(1L)).thenReturn(task);
        when(converter.convertToTaskDTO(task)).thenReturn(taskDTO);

        ResponseEntity<TaskDTO> response = taskController.getTaskById(1L);

        assertNotNull(response);
        assertEquals(taskDTO, response.getBody());
        verify(taskService, times(1)).getTaskById(1L);
    }

    @Test
    void getTaskById_ShouldReturnNotFound_WhenTaskDoesNotExist() {
        when(taskService.getTaskById(1L)).thenReturn(null);

        ResponseEntity<TaskDTO> response = taskController.getTaskById(1L);

        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void getAllTasks_ShouldReturnPagedResponse() {
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        PagedResponseDTO<TaskDTO> pagedResponseDTO =
                new PagedResponseDTO<>(List.of(taskDTO), 0, 10, 1, 1);

        when(taskService.getAllTasks(any(Pageable.class))).thenReturn(taskPage);
        when(converter.convertToTaskDTO(task)).thenReturn(taskDTO);

        ResponseEntity<PagedResponseDTO<TaskDTO>> response = taskController.getAllTasks(Pageable.unpaged());

        assertNotNull(response);
        assertEquals(pagedResponseDTO.getContent(), response.getBody().getContent());
        verify(taskService, times(1)).getAllTasks(any(Pageable.class));
    }
}