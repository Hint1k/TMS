package com.demo.tms.repository;

import com.demo.tms.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Custom query to find comments by task ID
    @Query("SELECT c FROM Comment c WHERE c.task.taskId = ?1")
    Page<Comment> findByTaskId(Long taskId, Pageable pageable);

    // Custom query to find comments by user ID
    @Query("SELECT c FROM Comment c WHERE c.user.userId = ?1")
    Page<Comment> findByUserId(Long userId, Pageable pageable);
}