package com.demo.tms.converter;

import com.demo.tms.dto.CommentDTO;
import com.demo.tms.dto.RoleDTO;
import com.demo.tms.dto.TaskDTO;
import com.demo.tms.dto.UserDTO;
import com.demo.tms.entity.Comment;
import com.demo.tms.entity.Role;
import com.demo.tms.entity.Task;
import com.demo.tms.entity.User;
import com.demo.tms.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ConverterImpl implements Converter {

    private final TaskService taskService;

    @Autowired
    public ConverterImpl(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public TaskDTO convertToTaskDTO(Task task) {
        if (task == null) return null;
        TaskDTO dto = new TaskDTO();
        dto.setTaskId(task.getTaskId());
        dto.setName(task.getName());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setAuthorId(task.getAuthor() != null ? task.getAuthor().getUserId() : null);
        dto.setAssigneeId(task.getAssignee() != null ? task.getAssignee().getUserId() : null);
        dto.setCommentIds(task.getComments() != null
                ? task.getComments().stream().map(Comment::getCommentId).collect(Collectors.toList())
                : new ArrayList<>());
        dto.setVersion(task.getVersion());
        return dto;
    }

    @Override
    public Task convertToTask(TaskDTO dto) {
        if (dto == null) return null;
        Task task = new Task();
        task.setTaskId(dto.getTaskId());
        task.setName(dto.getName());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setPriority(dto.getPriority());
        task.setAuthor(convertToUser(dto.getAuthorId()));
        task.setAssignee(convertToUser(dto.getAssigneeId()));
        task.setComments(dto.getCommentIds() != null
                ? dto.getCommentIds().stream().map(this::convertToComment).collect(Collectors.toList())
                : new ArrayList<>());
        task.setVersion(dto.getVersion());
        return task;
    }

    @Override
    public CommentDTO convertToCommentDTO(Comment comment) {
        if (comment == null) return null;
        CommentDTO dto = new CommentDTO();
        dto.setCommentId(comment.getCommentId());
        dto.setText(comment.getText());
        dto.setUserId(comment.getUser() != null ? comment.getUser().getUserId() : null);
        dto.setTaskId(comment.getTask() != null ? comment.getTask().getTaskId() : null);
        dto.setVersion(comment.getVersion());
        return dto;
    }

    @Override
    public Comment convertToComment(CommentDTO dto) {
        if (dto == null) return null;
        Comment comment = new Comment();
        comment.setCommentId(dto.getCommentId());
        comment.setText(dto.getText());
        comment.setUser(convertToUser(dto.getUserId()));
        if (dto.getTaskId() != null) {
            Task task = taskService.getTaskById(dto.getTaskId());
            if (task != null) {
                comment.setTask(task);
            }
        }
        comment.setVersion(dto.getVersion());
        return comment;
    }

    @Override
    public UserDTO convertToUserDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoleId(user.getRole() != null ? user.getRole().getRoleId() : null);
        dto.setCreatedTaskIds(user.getCreatedTasks() != null
                ? user.getCreatedTasks().stream().map(Task::getTaskId).collect(Collectors.toSet())
                : null);
        dto.setAssignedTaskIds(user.getAssignedTasks() != null
                ? user.getAssignedTasks().stream().map(Task::getTaskId).collect(Collectors.toSet())
                : null);
        dto.setEnabled(user.isEnabled());
        return dto;
    }

    @Override
    public User convertToUser(UserDTO dto) {
        if (dto == null) return null;
        User user = new User();
        user.setUserId(dto.getUserId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setRole(convertToRole(dto.getRoleId()));
        if (dto.getCreatedTaskIds() != null) {
            user.setCreatedTasks(dto.getCreatedTaskIds().stream()
                    .map(this::convertToTask).collect(Collectors.toSet()));
        }
        if (dto.getAssignedTaskIds() != null) {
            user.setAssignedTasks(dto.getAssignedTaskIds().stream()
                    .map(this::convertToTask).collect(Collectors.toSet()));
        }
        user.setEnabled(dto.isEnabled());
        return user;
    }

    @Override
    public RoleDTO convertToRoleDTO(Role role) {
        if (role == null) return null;
        RoleDTO dto = new RoleDTO();
        dto.setRoleId(role.getRoleId());
        dto.setAuthority(role.getAuthority());
        dto.setUserId(role.getUser() != null ? role.getUser().getUserId() : null);
        return dto;
    }

    @Override
    public Role convertToRole(RoleDTO dto) {
        if (dto == null) return null;
        Role role = new Role();
        role.setRoleId(dto.getRoleId());
        role.setAuthority(dto.getAuthority());
        role.setUser(convertToUser(dto.getUserId()));
        return role;
    }

    // Helper methods to convert from Long IDs to minimal entity objects.
    public User convertToUser(Long userId) {
        if (userId == null) return null;
        User user = new User();
        user.setUserId(userId);
        return user;
    }

    public Task convertToTask(Long taskId) {
        if (taskId == null) return null;
        Task task = new Task();
        task.setTaskId(taskId);
        return task;
    }

    public Role convertToRole(Long roleId) {
        if (roleId == null) return null;
        Role role = new Role();
        role.setRoleId(roleId);
        return role;
    }

    public Comment convertToComment(Long commentId) {
        if (commentId == null) return null;
        Comment comment = new Comment();
        comment.setCommentId(commentId);
        return comment;
    }
}