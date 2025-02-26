package com.demo.testclient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO implements Serializable {

    private Long roleId;

    @NotBlank(message = "Role is required") // Ensures it's not null or empty
    @Size(max = 45, message = "Role must not exceed 45 characters")
    private String authority;

    @NotNull(message = "User Id is required")
    private Long userId;

    // no roleId
    public RoleDTO(String authority, Long userId) {
        this.authority = authority;
        this.userId = userId;
    }
}