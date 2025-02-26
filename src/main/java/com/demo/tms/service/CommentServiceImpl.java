package com.demo.tms.service;

import com.demo.tms.entity.Comment;
import com.demo.tms.exception.OptimisticLockingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import com.demo.tms.exception.ResourceNotFoundException;
import com.demo.tms.repository.CommentRepository;
import com.demo.tms.repository.TaskRepository;
import com.demo.tms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code CommentServiceImpl} is the implementation of the {@link CommentService} interface.
 * <p>
 * This service handles the business logic related to {@link Comment} entities, including operations such as
 * saving, updating, deleting, and retrieving comments. It also supports caching, optimistic locking, and retry
 * mechanisms.
 * </p>
 */
@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    /**
     * Constructs a new {@code CommentServiceImpl} with the specified repositories.
     *
     * @param commentRepository the {@link CommentRepository} to interact with comment data
     * @param userRepository    the {@link UserRepository} to interact with user data
     * @param taskRepository    the {@link TaskRepository} to interact with task data
     */
    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, UserRepository userRepository,
                              TaskRepository taskRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    /**
     * Saves a new comment. This method is retried if an {@link OptimisticLockingException} is thrown.
     *
     * @param comment the {@link Comment} entity to be saved
     * @return the saved {@link Comment} entity
     */
    @Override
    @Transactional
    @Retryable(retryFor = OptimisticLockingException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
    public Comment saveComment(Comment comment) {
        validateCommentUsersAndTasks(comment);
        return commentRepository.save(comment);
    }

    /**
     * Updates an existing comment. This method evicts the comment from the cache and handles optimistic locking.
     *
     * @param commentId      the ID of the comment to be updated
     * @param updatedComment the updated {@link Comment} entity
     * @return the updated {@link Comment} entity
     * @throws OptimisticLockingException if the comment was updated by another transaction
     */
    @Override
    @Transactional
    @Retryable(retryFor = OptimisticLockingException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
    @CacheEvict(value = "comments", key = "#commentId")
    public Comment updateComment(Long commentId, Comment updatedComment) {
        try {
            Comment existingComment = commentRepository.findById(commentId).orElseThrow(() ->
                    new ResourceNotFoundException("Comment with ID " + commentId + " not found"));

            // validating user and task
            validateCommentUsersAndTasks(updatedComment);

            // Manual update of each field to avoid detached entity state
            existingComment.setText(updatedComment.getText());
            existingComment.setUser(updatedComment.getUser());
            existingComment.setText(updatedComment.getText());
            existingComment.setVersion(updatedComment.getVersion());

            return commentRepository.save(existingComment);
        } catch (OptimisticLockingException e) {
            log.warn("Comment was updated by another transaction: {}", e.getMessage());
            throw new OptimisticLockingException(
                    "Optimistic locking failure: Task was updated by another transaction." + e);
        }
    }

    /**
     * Deletes a comment by its ID. This method clears the cache for comments.
     *
     * @param commentId the ID of the comment to be deleted
     * @return {@code true} if the comment was successfully deleted, otherwise {@code false}
     */
    @Override
    @Transactional
    @CacheEvict(value = "comments", allEntries = true)
    public boolean deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment != null) {
            commentRepository.deleteById(commentId);
            return true;
        }
        return false;
    }

    /**
     * Retrieves a comment by its ID, utilizing caching.
     *
     * @param commentId the ID of the comment to retrieve
     * @return the {@link Comment} entity with the specified ID
     * @throws ResourceNotFoundException if the comment is not found
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "comments", key = "#commentId")
    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() ->
                new ResourceNotFoundException("Comment not found"));
    }

    /**
     * Retrieves all comments, paginated.
     *
     * @param pageable the {@link Pageable} object containing pagination information
     * @return a {@link Page} of {@link Comment} entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Comment> getAllComments(Pageable pageable) {
        return commentRepository.findAll(pageable);
    }

    /**
     * Retrieves comments for a specific task, paginated.
     *
     * @param taskId   the ID of the task for which to retrieve comments
     * @param pageable the {@link Pageable} object containing pagination information
     * @return a {@link Page} of {@link Comment} entities associated with the task
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "comments", key = "#taskId + '_task'")
    public Page<Comment> getCommentsByTask(Long taskId, Pageable pageable) {
        return commentRepository.findByTaskId(taskId, pageable);
    }

    /**
     * Retrieves comments by a specific user, paginated.
     *
     * @param userId   the ID of the user for which to retrieve comments
     * @param pageable the {@link Pageable} object containing pagination information
     * @return a {@link Page} of {@link Comment} entities created by the user
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "comments", key = "#userId + '_user_' + #pageable.pageNumber + '_' + #pageable.pageSize" +
            "+ '_' + #pageable.sort.toString()")
    public Page<Comment> getCommentsByUser(Long userId, Pageable pageable) {
        return commentRepository.findByUserId(userId, pageable);
    }

    /**
     * Validates that the {@link Comment} entity has valid user and task references.
     *
     * @param comment the {@link Comment} entity to validate
     * @throws ResourceNotFoundException if the user or task does not exist
     */
    private void validateCommentUsersAndTasks(Comment comment) {
        if (comment.getUser() != null && !userRepository.existsById(comment.getUser().getUserId())) {
            throw new ResourceNotFoundException("User with ID " + comment.getUser().getUserId() + " not found");
        }
        if (comment.getTask() != null && !taskRepository.existsById(comment.getTask().getTaskId())) {
            throw new ResourceNotFoundException("Task with ID " + comment.getTask().getTaskId() + " not found");
        }
    }
}