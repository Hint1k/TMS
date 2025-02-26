package com.demo.testclient.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Name is required")
    @Size(max = 45, message = "Name must not exceed 45 characters")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 68, message = "Password must be between 6 and 68 characters")
    private String password;

    @NotNull(message = "Role ID is required")
    private Long roleId;

    private Set<Long> createdTaskIds;
    private Set<Long> assignedTaskIds;
    private boolean isEnabled;

    // no userId
    public UserDTO(String name, String email, String password, Long roleId, Set<Long> createdTaskIds,
                   Set<Long> assignedTaskIds, boolean isEnabled) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.roleId = roleId;
        this.createdTaskIds = createdTaskIds;
        this.assignedTaskIds = assignedTaskIds;
        this.isEnabled = isEnabled;
    }
}