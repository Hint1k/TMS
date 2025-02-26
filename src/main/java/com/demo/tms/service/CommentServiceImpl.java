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

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, UserRepository userRepository,
                              TaskRepository taskRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    @Transactional
    @Retryable(retryFor = OptimisticLockingException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
    public Comment saveComment(Comment comment) {
        validateCommentUsersAndTasks(comment);
        return commentRepository.save(comment);
    }

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

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "comments", key = "#commentId")
    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() ->
                new ResourceNotFoundException("Comment not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Comment> getAllComments(Pageable pageable) {
        return commentRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "comments", key = "#taskId + '_task'")
    public Page<Comment> getCommentsByTask(Long taskId, Pageable pageable) {
        return commentRepository.findByTaskId(taskId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "comments", key = "#userId + '_user_' + #pageable.pageNumber + '_' + #pageable.pageSize" +
            "+ '_' + #pageable.sort.toString()")
    public Page<Comment> getCommentsByUser(Long userId, Pageable pageable) {
        return commentRepository.findByUserId(userId, pageable);
    }

    // Private helper method to validate user and task
    private void validateCommentUsersAndTasks(Comment comment) {
        if (comment.getUser() != null && !userRepository.existsById(comment.getUser().getUserId())) {
            throw new ResourceNotFoundException("User with ID " + comment.getUser().getUserId() + " not found");
        }
        if (comment.getTask() != null && !taskRepository.existsById(comment.getTask().getTaskId())) {
            throw new ResourceNotFoundException("Task with ID " + comment.getTask().getTaskId() + " not found");
        }
    }
}