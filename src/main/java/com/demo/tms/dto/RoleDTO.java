package com.demo.tms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * {@code RoleDTO} is a Data Transfer Object (DTO) used to represent a user's role and associated
 * information within the system. It contains details such as the role identifier, authority (role name),
 * and the user ID associated with the role.
 *
 * This DTO is used to transfer role-related data between different layers of the application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO implements Serializable {

    /**
     * The unique identifier for the role.
     * This field is typically used for database lookups and identifying the role within the system.
     */
    private Long roleId;

    /**
     * The authority (name) of the role.
     * This field is required and should not exceed 45 characters. It represents the role's name,
     * such as "ADMIN", "USER", etc.
     */
    @NotBlank(message = "Role is required") // Ensures it's not null or empty
    @Size(max = 45, message = "Role must not exceed 45 characters")
    private String authority;

    /**
     * The unique identifier for the user associated with this role.
     * This field links the role to a specific user within the system.
     */
    @NotNull(message = "User Id is required")
    private Long userId;

    /**
     * Constructor without roleId, used for cases where the roleId is not required (e.g., role creation).
     *
     * @param authority The authority (role name) associated with the role.
     * @param userId The user ID associated with the role.
     */
    public RoleDTO(String authority, Long userId) {
        this.authority = authority;
        this.userId = userId;
    }
}