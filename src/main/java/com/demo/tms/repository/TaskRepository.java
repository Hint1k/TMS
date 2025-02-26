package com.demo.tms.repository;

import com.demo.tms.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE t.author.userId = ?1")
    Page<Task> findByAuthorId(Long authorId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.assignee.userId = ?1")
    Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable);

    @Query("SELECT COUNT(t) > 0 FROM Task t WHERE t.taskId = ?1 AND t.assignee.userId = ?2")
    boolean existsByTaskIdAndAssigneeId(Long taskId, Long userId);

    @Query("SELECT COUNT(t) > 0 FROM Task t WHERE t.taskId = ?1 AND t.author.userId = ?2")
    boolean existsByTaskIdAndAuthorId(Long taskId, Long userId);
}