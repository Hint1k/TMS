package com.demo.tms.converter;

import com.demo.tms.dto.CommentDTO;
import com.demo.tms.dto.RoleDTO;
import com.demo.tms.dto.TaskDTO;
import com.demo.tms.dto.UserDTO;
import com.demo.tms.entity.Comment;
import com.demo.tms.entity.Role;
import com.demo.tms.entity.Task;
import com.demo.tms.entity.User;

public interface Converter {

    TaskDTO convertToTaskDTO(Task task);

    Task convertToTask(TaskDTO dto);

    CommentDTO convertToCommentDTO(Comment comment);

    Comment convertToComment(CommentDTO dto);

    UserDTO convertToUserDTO(User user);

    User convertToUser(UserDTO dto);

    RoleDTO convertToRoleDTO(Role role);

    Role convertToRole(RoleDTO dto);
}