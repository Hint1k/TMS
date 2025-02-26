package com.demo.tms.service;

import com.demo.tms.entity.Role;
import com.demo.tms.entity.Task;
import com.demo.tms.entity.User;
import com.demo.tms.exception.OptimisticLockingException;
import com.demo.tms.exception.ResourceNotFoundException;
import com.demo.tms.repository.TaskRepository;
import com.demo.tms.repository.UserRepository;
import com.demo.tms.utils.TaskPriority;
import com.demo.tms.utils.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task task;
    private Task updatedTask;
    private Long taskId;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        Role role1 = new Role();
        user1 = new User();
        role1.setRoleId(1L);
        role1.setAuthority("ROLE_ADMIN");
        role1.setUser(user1);
        user1.setUserId(1L);
        user1.setUsername("admin");
        user1.setPassword("password1");
        user1.setEmail("admin@gmail.com");
        user1.setRole(role1);
        user1.setEnabled(true);

        taskId = 1L;
        task = new Task();
        task.setTaskId(taskId);
        task.setName("Test Task");
        task.setDescription("This is a test task");
        task.setStatus(TaskStatus.PENDING);
        task.setPriority(TaskPriority.HIGH);
        task.setAuthor(user1);
        task.setAssignee(user1);

        updatedTask = new Task();
        updatedTask.setTaskId(taskId);
        updatedTask.setName("Updated Task");
        updatedTask.setDescription("This is an updated test task");
        updatedTask.setStatus(TaskStatus.PROCESSING);
        updatedTask.setPriority(TaskPriority.LOW);
        updatedTask.setAuthor(user1);
        updatedTask.setAssignee(user1);
    }

    @Test
    void testSaveTask_Success() {
        // Mock repository save behavior
        when(userRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task savedTask = taskService.saveTask(task);

        assertNotNull(savedTask);
        assertEquals(taskId, savedTask.getTaskId());
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(userRepository, times(2)).existsById(1L);
    }

    @Test
    void testUpdateTask_Success() {
        // Mock task retrieval and save behavior
        when(userRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findById(taskId)).thenReturn(java.util.Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        Task updated = taskService.updateTask(taskId, updatedTask);

        assertNotNull(updated);
        assertEquals(updatedTask.getName(), updated.getName());
        assertEquals(updatedTask.getDescription(), updated.getDescription());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(userRepository, times(2)).existsById(1L);
    }

    @Test
    void testUpdateTask_TaskNotFound() {
        // Mock task retrieval to return empty
        when(taskRepository.findById(taskId)).thenReturn(java.util.Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.updateTask(taskId, updatedTask);
        });

        assertEquals("Task with ID " + taskId + " not found", exception.getMessage());
    }

    @Test
    void testUpdateTask_OptimisticLockingException() {
        String message = "Optimistic locking failure: Task was updated by another transaction";

        // Mock task retrieval
        when(userRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findById(taskId)).thenReturn(java.util.Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenThrow(new OptimisticLockingException(message));

        // Trigger the exception and capture it
        OptimisticLockingException exception = assertThrows(OptimisticLockingException.class, () -> {
            taskService.updateTask(taskId, updatedTask);
        });

        // Assert the message contains the expected text
        assertTrue(exception.getMessage().contains(message));
    }

    @Test
    void testDeleteTask_Success() {
        // Mock task retrieval and delete behavior
        when(taskRepository.findById(taskId)).thenReturn(java.util.Optional.of(task));

        boolean isDeleted = taskService.deleteTask(taskId);

        assertTrue(isDeleted);
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).deleteById(taskId);
    }

    @Test
    void testDeleteTask_TaskNotFound() {
        // Mock task retrieval to return empty
        when(taskRepository.findById(taskId)).thenReturn(java.util.Optional.empty());

        boolean isDeleted = taskService.deleteTask(taskId);

        assertFalse(isDeleted);
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(0)).deleteById(taskId);
    }

    @Test
    void testGetTaskById_Success() {
        // Mock task retrieval
        when(taskRepository.findById(taskId)).thenReturn(java.util.Optional.of(task));

        Task foundTask = taskService.getTaskById(taskId);

        assertNotNull(foundTask);
        assertEquals(taskId, foundTask.getTaskId());
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    void testGetTaskById_TaskNotFound() {
        // Mock task retrieval to return empty
        when(taskRepository.findById(taskId)).thenReturn(java.util.Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.getTaskById(taskId);
        });

        assertEquals("Task not found", exception.getMessage());
    }

    @Test
    void testGetAllTasks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = mock(Page.class);

        when(taskRepository.findAll(pageable)).thenReturn(taskPage);

        Page<Task> tasks = taskService.getAllTasks(pageable);

        assertNotNull(tasks);
        verify(taskRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetTasksByAuthor() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = mock(Page.class);
        when(taskRepository.findByAuthorId(anyLong(), eq(pageable))).thenReturn(taskPage);

        Page<Task> tasks = taskService.getTasksByAuthor(1L, pageable);

        assertNotNull(tasks);
        verify(taskRepository, times(1)).findByAuthorId(anyLong(), eq(pageable));
    }

    @Test
    void testGetTasksByAssignee() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = mock(Page.class);
        when(taskRepository.findByAssigneeId(anyLong(), eq(pageable))).thenReturn(taskPage);

        Page<Task> tasks = taskService.getTasksByAssignee(2L, pageable);

        assertNotNull(tasks);
        verify(taskRepository, times(1)).findByAssigneeId(anyLong(), eq(pageable));
    }

    @Test
    void testValidateTaskUsers_AssigneeNotFound() {
        user2 = new User();
        task.setAssignee(user2);  // Assignee does not exist

        // Mock the user repository behavior
        when(userRepository.existsById(user2.getUserId())).thenReturn(false); // Assignee does not exist

        // Call saveTask, which will invoke validateTaskUsers
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.saveTask(task);
        });

        // Assert that the exception message matches the expected output for the assignee not being found
        assertEquals("Assignee with ID " + user2.getUserId() + " not found", exception.getMessage());
    }

    @Test
    void testValidateTaskUsers_AuthorNotFound() {
        user2 = new User();
        task.setAuthor(user2);    // Author does not exist

        // Mock the user repository behavior
        when(userRepository.existsById(user1.getUserId())).thenReturn(true);  // Assignee exists
        when(userRepository.existsById(user2.getUserId())).thenReturn(false); // Author does not exist

        // Call saveTask, which will invoke validateTaskUsers
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.saveTask(task);
        });

        // Assert that the exception message matches the expected output for the author not being found
        assertEquals("Author with ID " + user2.getUserId() + " not found", exception.getMessage());
    }
}