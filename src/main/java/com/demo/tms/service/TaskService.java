package com.demo.tms.service;

import com.demo.tms.entity.Task;
import com.demo.tms.utils.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    Task saveTask(Task task);

    Task updateTask(Long taskId, Task updatedTask);

    Task updateTaskStatus (Long taskId, TaskStatus newStatus);

    boolean deleteTask(Long taskId);

    Task getTaskById(Long taskId);

    Page<Task> getAllTasks(Pageable pageable);

    Page<Task> getTasksByAuthor(Long authorId, Pageable pageable);

    Page<Task> getTasksByAssignee(Long assigneeId, Pageable pageable);
}