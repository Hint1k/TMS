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

/**
 * {@code ConverterImpl} is the implementation of the {@link Converter} interface.
 * It provides methods for converting between entity and DTO (Data Transfer Object)
 * objects for various entities such as {@link Task}, {@link Comment}, {@link User},
 * and {@link Role}. The class utilizes the {@link TaskService} to fetch associated
 * entities during conversions, such as converting a {@link CommentDTO} to a {@link Comment}
 * entity and linking it to the correct {@link Task}.
 */
@Component
@Slf4j
public class ConverterImpl implements Converter {

    private final TaskService taskService;

    /**
     * Constructs a {@code ConverterImpl} with the specified {@link TaskService}.
     *
     * @param taskService The {@link TaskService} to interact with for task-related conversions.
     */
    @Autowired
    public ConverterImpl(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Converts a {@link Task} entity to a {@link TaskDTO}.
     *
     * @param task The {@link Task} entity to convert.
     * @return The corresponding {@link TaskDTO}.
     */
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

    /**
     * Converts a {@link TaskDTO} to a {@link Task} entity.
     *
     * @param dto The {@link TaskDTO} to convert.
     * @return The corresponding {@link Task} entity.
     */
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

    /**
     * Converts a {@link Comment} entity to a {@link CommentDTO}.
     *
     * @param comment The {@link Comment} entity to convert.
     * @return The corresponding {@link CommentDTO}.
     */
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

    /**
     * Converts a {@link CommentDTO} to a {@link Comment} entity.
     *
     * @param dto The {@link CommentDTO} to convert.
     * @return The corresponding {@link Comment} entity.
     */
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

    /**
     * Converts a {@link User} entity to a {@link UserDTO}.
     *
     * @param user The {@link User} entity to convert.
     * @return The corresponding {@link UserDTO}.
     */
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

    /**
     * Converts a {@link UserDTO} to a {@link User} entity.
     *
     * @param dto The {@link UserDTO} to convert.
     * @return The corresponding {@link User} entity.
     */
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

    /**
     * Converts a {@link Role} entity to a {@link RoleDTO}.
     *
     * @param role The {@link Role} entity to convert.
     * @return The corresponding {@link RoleDTO}.
     */
    @Override
    public RoleDTO convertToRoleDTO(Role role) {
        if (role == null) return null;
        RoleDTO dto = new RoleDTO();
        dto.setRoleId(role.getRoleId());
        dto.setAuthority(role.getAuthority());
        dto.setUserId(role.getUser() != null ? role.getUser().getUserId() : null);
        return dto;
    }

    /**
     * Converts a {@link RoleDTO} to a {@link Role} entity.
     *
     * @param dto The {@link RoleDTO} to convert.
     * @return The corresponding {@link Role} entity.
     */
    @Override
    public Role convertToRole(RoleDTO dto) {
        if (dto == null) return null;
        Role role = new Role();
        role.setRoleId(dto.getRoleId());
        role.setAuthority(dto.getAuthority());
        role.setUser(convertToUser(dto.getUserId()));
        return role;
    }

    /**
     * Converts a {@link Long} userId to a {@link User} entity.
     *
     * @param userId The ID of the user.
     * @return The corresponding {@link User} entity.
     */
    public User convertToUser(Long userId) {
        if (userId == null) return null;
        User user = new User();
        user.setUserId(userId);
        return user;
    }

    /**
     * Converts a {@link Long} taskId to a {@link Task} entity.
     *
     * @param taskId The ID of the task.
     * @return The corresponding {@link Task} entity.
     */
    public Task convertToTask(Long taskId) {
        if (taskId == null) return null;
        Task task = new Task();
        task.setTaskId(taskId);
        return task;
    }

    /**
     * Converts a {@link Long} roleId to a {@link Role} entity.
     *
     * @param roleId The ID of the role.
     * @return The corresponding {@link Role} entity.
     */
    public Role convertToRole(Long roleId) {
        if (roleId == null) return null;
        Role role = new Role();
        role.setRoleId(roleId);
        return role;
    }

    /**
     * Converts a {@link Long} commentId to a {@link Comment} entity.
     *
     * @param commentId The ID of the comment.
     * @return The corresponding {@link Comment} entity.
     */
    public Comment convertToComment(Long commentId) {
        if (commentId == null) return null;
        Comment comment = new Comment();
        comment.setCommentId(commentId);
        return comment;
    }
}