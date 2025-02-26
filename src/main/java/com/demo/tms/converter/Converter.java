package com.demo.tms.converter;

import com.demo.tms.dto.CommentDTO;
import com.demo.tms.dto.RoleDTO;
import com.demo.tms.dto.TaskDTO;
import com.demo.tms.dto.UserDTO;
import com.demo.tms.entity.Comment;
import com.demo.tms.entity.Role;
import com.demo.tms.entity.Task;
import com.demo.tms.entity.User;

/**
 * {@code Converter} interface defines the contract for converting between
 * entity and DTO (Data Transfer Object) objects in the application.
 * These conversions are essential for transferring data between layers
 * (e.g., service layer, controller layer) and ensuring proper encapsulation.
 */
public interface Converter {

    /**
     * Converts a {@link Task} entity to a {@link TaskDTO}.
     *
     * @param task The {@link Task} entity to convert.
     * @return The corresponding {@link TaskDTO}.
     */
    TaskDTO convertToTaskDTO(Task task);

    /**
     * Converts a {@link TaskDTO} to a {@link Task} entity.
     *
     * @param dto The {@link TaskDTO} to convert.
     * @return The corresponding {@link Task} entity.
     */
    Task convertToTask(TaskDTO dto);

    /**
     * Converts a {@link Comment} entity to a {@link CommentDTO}.
     *
     * @param comment The {@link Comment} entity to convert.
     * @return The corresponding {@link CommentDTO}.
     */
    CommentDTO convertToCommentDTO(Comment comment);

    /**
     * Converts a {@link CommentDTO} to a {@link Comment} entity.
     *
     * @param dto The {@link CommentDTO} to convert.
     * @return The corresponding {@link Comment} entity.
     */
    Comment convertToComment(CommentDTO dto);

    /**
     * Converts a {@link User} entity to a {@link UserDTO}.
     *
     * @param user The {@link User} entity to convert.
     * @return The corresponding {@link UserDTO}.
     */
    UserDTO convertToUserDTO(User user);

    /**
     * Converts a {@link UserDTO} to a {@link User} entity.
     *
     * @param dto The {@link UserDTO} to convert.
     * @return The corresponding {@link User} entity.
     */
    User convertToUser(UserDTO dto);

    /**
     * Converts a {@link Role} entity to a {@link RoleDTO}.
     *
     * @param role The {@link Role} entity to convert.
     * @return The corresponding {@link RoleDTO}.
     */
    RoleDTO convertToRoleDTO(Role role);

    /**
     * Converts a {@link RoleDTO} to a {@link Role} entity.
     *
     * @param dto The {@link RoleDTO} to convert.
     * @return The corresponding {@link Role} entity.
     */
    Role convertToRole(RoleDTO dto);
}