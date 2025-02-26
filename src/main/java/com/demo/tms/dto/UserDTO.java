package com.demo.tms.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO implements Serializable {

    private Long userId;

    @NotBlank(message = "Username is required")
    @Size(max = 45, message = "Username must not exceed 45 characters")
    private String username;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 68, message = "Password must be between 6 and 68 characters")
    private String password;

    private Long roleId;

    private Set<Long> createdTaskIds;
    private Set<Long> assignedTaskIds;
    private boolean isEnabled;

    // no userId
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