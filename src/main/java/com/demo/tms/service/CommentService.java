package com.demo.tms.service;

import com.demo.tms.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    Comment saveComment(Comment comment);

    Comment updateComment(Long commentId, Comment updatedComment);

    boolean deleteComment(Long commentId);

    Comment getCommentById(Long commentId);

    Page<Comment> getAllComments(Pageable pageable);

    Page<Comment> getCommentsByTask(Long taskId, Pageable pageable);

    Page<Comment> getCommentsByUser(Long userId, Pageable pageable);
}