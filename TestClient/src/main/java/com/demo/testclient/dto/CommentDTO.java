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
public class CommentDTO implements Serializable {

    private Long commentId;

    @NotBlank(message = "Text of a comment is required")
    @Size(max = 1000, message = "Comment text must not exceed 1000 characters")
    private String text;

    @NotNull(message = "User Id is required")
    private Long userId;

    @NotNull(message = "Task Id is required")
    private Long taskId;

    // no commentId
    public CommentDTO(String text, Long userId, Long taskId) {
        this.text = text;
        this.userId = userId;
        this.taskId = taskId;
    }
}