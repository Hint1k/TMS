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

@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    @Retryable(retryFor = OptimisticLockingException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
    public Task saveTask(Task task) {
        validateTaskUsers(task);
        return taskRepository.save(task);
    }

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

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "#taskId")
    public Task getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // Force loading of comments before caching to avoid LazyInitialization Exception
        task.getComments().size();

        return task;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "#authorId + '_author_' + #pageable.pageNumber + '_' + #pageable.pageSize " +
            "+ '_' + #pageable.sort.toString()")
    public Page<Task> getTasksByAuthor(Long authorId, Pageable pageable) {
        return taskRepository.findByAuthorId(authorId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "#assigneeId + '_assignee_' + #pageable.pageNumber + '_' + #pageable.pageSize" +
            "+ '_' + #pageable.sort.toString()")
    public Page<Task> getTasksByAssignee(Long assigneeId, Pageable pageable) {
        return taskRepository.findByAssigneeId(assigneeId, pageable);
    }

    // Private helper method to validate assignee and author
    private void validateTaskUsers(Task task) {
        if (task.getAssignee() != null && !userRepository.existsById(task.getAssignee().getUserId())) {
            throw new ResourceNotFoundException("Assignee with ID " + task.getAssignee().getUserId() + " not found");
        }
        if (task.getAuthor() != null && !userRepository.existsById(task.getAuthor().getUserId())) {
            throw new ResourceNotFoundException("Author with ID " + task.getAuthor().getUserId() + " not found");
        }
    }
}