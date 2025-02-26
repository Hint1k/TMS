package com.demo.tms.service;

import com.demo.tms.entity.Task;
import com.demo.tms.utils.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * {@code TaskService} defines the contract for managing {@link Task} entities.
 * <p>
 * This interface provides methods for creating, updating, deleting, and retrieving tasks,
 * as well as updating their statuses. It allows interacting with tasks within the system,
 * whether by their author, assignee, or through general task management operations.
 * </p>
 */
public interface TaskService {

    /**
     * Saves a new task.
     *
     * @param task the {@link Task} entity to be saved
     * @return the saved {@link Task} entity
     */
    Task saveTask(Task task);

    /**
     * Updates an existing task by its ID.
     * If the task with the specified ID does not exist, an exception is thrown.
     *
     * @param taskId      the ID of the task to be updated
     * @param updatedTask the updated {@link Task} entity
     * @return the updated {@link Task} entity
     */
    Task updateTask(Long taskId, Task updatedTask);

    /**
     * Updates the status of an existing task by its ID.
     * If the task with the specified ID does not exist, an exception is thrown.
     *
     * @param taskId    the ID of the task whose status is to be updated
     * @param newStatus the new {@link TaskStatus} to set
     * @return the updated {@link Task} entity
     */
    Task updateTaskStatus(Long taskId, TaskStatus newStatus);

    /**
     * Deletes a task by its ID.
     *
     * @param taskId the ID of the task to be deleted
     * @return {@code true} if the task was successfully deleted, otherwise {@code false}
     */
    boolean deleteTask(Long taskId);

    /**
     * Retrieves a task by its ID.
     *
     * @param taskId the ID of the task to retrieve
     * @return the {@link Task} entity with the specified ID
     */
    Task getTaskById(Long taskId);

    /**
     * Retrieves all tasks in the system.
     *
     * @param pageable the pagination information
     * @return a {@link Page} of all {@link Task} entities
     */
    Page<Task> getAllTasks(Pageable pageable);

    /**
     * Retrieves tasks associated with a specific author.
     *
     * @param authorId the ID of the author whose tasks are to be retrieved
     * @param pageable the pagination information
     * @return a {@link Page} of {@link Task} entities associated with the specified author
     */
    Page<Task> getTasksByAuthor(Long authorId, Pageable pageable);

    /**
     * Retrieves tasks assigned to a specific assignee.
     *
     * @param assigneeId the ID of the assignee whose tasks are to be retrieved
     * @param pageable   the pagination information
     * @return a {@link Page} of {@link Task} entities assigned to the specified assignee
     */
    Page<Task> getTasksByAssignee(Long assigneeId, Pageable pageable);
}