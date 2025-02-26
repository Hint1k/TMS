package com.demo.tms.service;

import com.demo.tms.exception.OptimisticLockingException;
import com.demo.tms.exception.ResourceNotFoundException;
import com.demo.tms.repository.TaskRepository;
import com.demo.tms.entity.Task;
import com.demo.tms.utils.TaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import com.demo.tms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code TaskServiceImpl} is the implementation of the {@link TaskService} interface.
 * <p>
 * This service handles business logic related to {@link Task} entities, including operations such as
 * saving, updating, deleting, and retrieving tasks. It also ensures that the assignee and author of a task
 * exist before performing any operations.
 * </p>
 */
@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    /**
     * Constructs a new {@code TaskServiceImpl} with the specified repositories.
     *
     * @param taskRepository the {@link TaskRepository} to interact with task data
     * @param userRepository the {@link UserRepository} to interact with user data
     */
    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    /**
     * Saves a new task after validating that the assignee and author exist.
     *
     * @param task the {@link Task} entity to be saved
     * @return the saved {@link Task} entity
     */
    @Override
    @Transactional
    @Retryable(retryFor = OptimisticLockingException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
    public Task saveTask(Task task) {
        validateTaskUsers(task);
        return taskRepository.save(task);
    }

    /**
     * Updates an existing task. If the task does not exist, an exception is thrown.
     * The assignee and author are validated before updating.
     *
     * @param taskId      the ID of the task to be updated
     * @param updatedTask the updated {@link Task} entity
     * @return the updated {@link Task} entity
     * @throws ResourceNotFoundException if the task with the specified ID is not found
     */
    @Override
    @Transactional
    @Retryable(retryFor = OptimisticLockingException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
    @CacheEvict(value = "tasks", key = "#taskId")
    public Task updateTask(Long taskId, Task updatedTask) {
        try {
            Task existingTask = taskRepository.findById(taskId).orElseThrow(() ->
                    new ResourceNotFoundException("Task with ID " + taskId + " not found"));

            // Validate assignee and author before updating
            validateTaskUsers(updatedTask);

            // Manual update of each field to avoid detached entity state
            existingTask.setName(updatedTask.getName());
            existingTask.setDescription(updatedTask.getDescription());
            existingTask.setStatus(updatedTask.getStatus());
            existingTask.setPriority(updatedTask.getPriority());
            existingTask.setAuthor(updatedTask.getAuthor());
            existingTask.setAssignee(updatedTask.getAssignee());
            existingTask.setVersion(updatedTask.getVersion());

            return taskRepository.save(existingTask);
        } catch (OptimisticLockingException e) {
            log.warn("Task was updated by another transaction: {}", e.getMessage());
            throw new OptimisticLockingException(
                    "Optimistic locking failure: Task was updated by another transaction." + e);
        }
    }

    /**
     * Updates the status of an existing task. If the task does not exist, an exception is thrown.
     *
     * @param taskId    the ID of the task to be updated
     * @param newStatus the new status of the task
     * @return the updated {@link Task} entity
     * @throws OptimisticLockingException if the task was modified by another transaction
     */
    @Override
    @Transactional
    @CacheEvict(value = "tasks", key = "#taskId")
    public Task updateTaskStatus(Long taskId, TaskStatus newStatus) {
        try {
            Task existingTask = taskRepository.findById(taskId).orElseThrow(() ->
                    new ResourceNotFoundException("Task with ID " + taskId + " not found"));

            existingTask.setStatus(newStatus);
            return taskRepository.save(existingTask);
        } catch (OptimisticLockingException e) {
            log.warn("Optimistic locking failure while updating task {}: {}", taskId, e.getMessage());
            throw new OptimisticLockingException("Task was modified by another transaction. Please retry.");
        }
    }

    /**
     * Deletes a task by its ID.
     *
     * @param taskId the ID of the task to be deleted
     * @return {@code true} if the task was successfully deleted, otherwise {@code false}
     */
    @Override
    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public boolean deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task != null) {
            taskRepository.deleteById(taskId);
            return true;
        }
        return false;
    }

    /**
     * Retrieves a task by its ID.
     *
     * @param taskId the ID of the task to retrieve
     * @return the {@link Task} entity with the specified ID
     * @throws ResourceNotFoundException if the task is not found
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "#taskId")
    public Task getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // Force loading of comments before caching to avoid LazyInitialization Exception
        task.getComments().size();

        return task;
    }

    /**
     * Retrieves all tasks with pagination.
     *
     * @param pageable the pagination information
     * @return a {@link Page} of {@link Task} entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Task> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    /**
     * Retrieves tasks assigned to a specific author with pagination.
     *
     * @param authorId the ID of the author
     * @param pageable the pagination information
     * @return a {@link Page} of {@link Task} entities assigned to the specified author
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "#authorId + '_author_' + #pageable.pageNumber + '_' + #pageable.pageSize " +
            "+ '_' + #pageable.sort.toString()")
    public Page<Task> getTasksByAuthor(Long authorId, Pageable pageable) {
        return taskRepository.findByAuthorId(authorId, pageable);
    }

    /**
     * Retrieves tasks assigned to a specific assignee with pagination.
     *
     * @param assigneeId the ID of the assignee
     * @param pageable   the pagination information
     * @return a {@link Page} of {@link Task} entities assigned to the specified assignee
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "#assigneeId + '_assignee_' + #pageable.pageNumber + '_' + #pageable.pageSize" +
            "+ '_' + #pageable.sort.toString()")
    public Page<Task> getTasksByAssignee(Long assigneeId, Pageable pageable) {
        return taskRepository.findByAssigneeId(assigneeId, pageable);
    }

    /**
     * Validates that the assignee and author associated with the task exist.
     *
     * @param task the {@link Task} entity to validate
     * @throws ResourceNotFoundException if the assignee or author does not exist
     */
    private void validateTaskUsers(Task task) {
        if (task.getAssignee() != null && !userRepository.existsById(task.getAssignee().getUserId())) {
            throw new ResourceNotFoundException("Assignee with ID " + task.getAssignee().getUserId() + " not found");
        }
        if (task.getAuthor() != null && !userRepository.existsById(task.getAuthor().getUserId())) {
            throw new ResourceNotFoundException("Author with ID " + task.getAuthor().getUserId() + " not found");
        }
    }
}