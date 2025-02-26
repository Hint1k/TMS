package com.demo.tms.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * {@code UserDTO} is a Data Transfer Object (DTO) used to represent a user's information within the system.
 * It includes details such as the user's identifier, username, email, password, role, tasks the user created
 * or is assigned to, and their enabled status. This DTO is primarily used to transfer user-related data between
 * different layers of the application.
 * <p>
 * This DTO includes validation constraints to ensure that the user information meets required criteria,
 * such as non-null fields, valid email format, and appropriate password length.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO implements Serializable {

    /**
     * The unique identifier for the user.
     * This field is used for database lookups and identifying the user within the system.
     */
    private Long userId;

    /**
     * The username of the user.
     * This field is required and must not exceed 45 characters.
     * It represents the name used for authentication and identification.
     */
    @NotBlank(message = "Username is required")
    @Size(max = 45, message = "Username must not exceed 45 characters")
    private String username;

    /**
     * The email address of the user.
     * This field is required, must be a valid email format, and must not exceed 100 characters.
     * It is used for user communication and notifications.
     */
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    /**
     * The password of the user.
     * This field is required and must be between 6 and 68 characters.
     * The password is used for user authentication.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 68, message = "Password must be between 6 and 68 characters")
    private String password;

    /**
     * The unique identifier of the user's role.
     * This field links the user to a specific role, which governs access and permissions.
     */
    private Long roleId;

    /**
     * A set of task IDs that the user has created.
     * This field can be empty if the user has not created any tasks.
     */
    private Set<Long> createdTaskIds;

    /**
     * A set of task IDs that the user is assigned to.
     * This field can be empty if the user is not assigned to any tasks.
     */
    private Set<Long> assignedTaskIds;

    /**
     * A flag indicating whether the user's account is enabled or disabled.
     * This field is used to activate or deactivate the user's access to the system.
     */
    private boolean isEnabled;

    /**
     * Constructor without userId, used for cases where the userId is not required (e.g., user creation).
     *
     * @param username The username of the user.
     * @param email The email address of the user.
     * @param password The password of the user.
     * @param roleId The ID of the role assigned to the user.
     * @param createdTaskIds The set of task IDs that the user has created.
     * @param assignedTaskIds The set of task IDs that the user is assigned to.
     * @param isEnabled The enabled status of the user's account.
     */
    public UserDTO(String username, String email, String password, Long roleId, Set<Long> createdTaskIds,
                   Set<Long> assignedTaskIds, boolean isEnabled) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.roleId = roleId;
        this.createdTaskIds = createdTaskIds;
        this.assignedTaskIds = assignedTaskIds;
        this.isEnabled = isEnabled;
    }
}