package com.demo.tms.service;

import com.demo.tms.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * {@code CommentService} provides an interface for performing operations related to {@link Comment} entities.
 * <p>
 * It defines methods for creating, updating, deleting, and retrieving comments, as well as paginated retrieval
 * of comments by task or user.
 * </p>
 */
public interface CommentService {

    /**
     * Saves a new comment.
     *
     * @param comment the {@link Comment} entity to be saved
     * @return the saved {@link Comment} entity
     */
    Comment saveComment(Comment comment);

    /**
     * Updates an existing comment.
     *
     * @param commentId the ID of the comment to be updated
     * @param updatedComment the updated {@link Comment} entity
     * @return the updated {@link Comment} entity
     */
    Comment updateComment(Long commentId, Comment updatedComment);

    /**
     * Deletes a comment by its ID.
     *
     * @param commentId the ID of the comment to be deleted
     * @return {@code true} if the comment was successfully deleted, otherwise {@code false}
     */
    boolean deleteComment(Long commentId);

    /**
     * Retrieves a comment by its ID.
     *
     * @param commentId the ID of the comment to retrieve
     * @return the {@link Comment} entity with the specified ID
     */
    Comment getCommentById(Long commentId);

    /**
     * Retrieves all comments with pagination.
     *
     * @param pageable the {@link Pageable} object containing pagination information
     * @return a {@link Page} of {@link Comment} entities
     */
    Page<Comment> getAllComments(Pageable pageable);

    /**
     * Retrieves comments for a specific task with pagination.
     *
     * @param taskId the ID of the task for which to retrieve comments
     * @param pageable the {@link Pageable} object containing pagination information
     * @return a {@link Page} of {@link Comment} entities associated with the task
     */
    Page<Comment> getCommentsByTask(Long taskId, Pageable pageable);

    /**
     * Retrieves comments by a specific user with pagination.
     *
     * @param userId the ID of the user for which to retrieve comments
     * @param pageable the {@link Pageable} object containing pagination information
     * @return a {@link Page} of {@link Comment} entities created by the user
     */
    Page<Comment> getCommentsByUser(Long userId, Pageable pageable);
}