package com.demo.tms.repository;

import com.demo.tms.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * {@code CommentRepository} is a Spring Data JPA repository interface for performing CRUD operations
 * and custom queries related to {@link Comment} entities in the database.
 * <p>
 * This repository provides methods to retrieve comments based on task and user IDs, with support for pagination.
 * </p>
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Finds comments associated with a specific task, with support for pagination.
     *
     * @param taskId The ID of the task for which comments are to be fetched.
     * @param pageable The pagination information (page number, size, etc.).
     * @return A page of comments associated with the given task ID.
     */
    @Query("SELECT c FROM Comment c WHERE c.task.taskId = ?1")
    Page<Comment> findByTaskId(Long taskId, Pageable pageable);

    /**
     * Finds comments created by a specific user, with support for pagination.
     *
     * @param userId The ID of the user whose comments are to be fetched.
     * @param pageable The pagination information (page number, size, etc.).
     * @return A page of comments created by the given user ID.
     */
    @Query("SELECT c FROM Comment c WHERE c.user.userId = ?1")
    Page<Comment> findByUserId(Long userId, Pageable pageable);
}