package com.demo.tms.repository;

import com.demo.tms.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;

/**
 * {@code TaskRepository} is a Spring Data JPA repository interface for performing CRUD operations
 * related to {@link Task} entities in the database.
 * <p>
 * This repository provides methods to find tasks by author or assignee, check if a task exists
 * with a specific author or assignee, and paginate the results for both author and assignee tasks.
 * </p>
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Finds tasks assigned to a specific author identified by their {@code authorId}.
     *
     * @param authorId the ID of the author
     * @param pageable the pagination information
     * @return a {@link Page} of {@link Task} assigned to the given author
     */
    @Query("SELECT t FROM Task t WHERE t.author.userId = ?1")
    Page<Task> findByAuthorId(Long authorId, Pageable pageable);

    /**
     * Finds tasks assigned to a specific assignee identified by their {@code assigneeId}.
     *
     * @param assigneeId the ID of the assignee
     * @param pageable the pagination information
     * @return a {@link Page} of {@link Task} assigned to the given assignee
     */
    @Query("SELECT t FROM Task t WHERE t.assignee.userId = ?1")
    Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable);

    /**
     * Checks whether a task with a specific ID exists and is assigned to a specific user identified
     * by their {@code userId}.
     *
     * @param taskId the ID of the task
     * @param userId the ID of the user
     * @return {@code true} if the task exists and is assigned to the user, {@code false} otherwise
     */
    @Query("SELECT COUNT(t) > 0 FROM Task t WHERE t.taskId = ?1 AND t.assignee.userId = ?2")
    boolean existsByTaskIdAndAssigneeId(Long taskId, Long userId);

    /**
     * Checks whether a task with a specific ID exists and is created by a specific user identified
     * by their {@code userId}.
     *
     * @param taskId the ID of the task
     * @param userId the ID of the user
     * @return {@code true} if the task exists and was created by the user, {@code false} otherwise
     */
    @Query("SELECT COUNT(t) > 0 FROM Task t WHERE t.taskId = ?1 AND t.author.userId = ?2")
    boolean existsByTaskIdAndAuthorId(Long taskId, Long userId);
}