package com.demo.tms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * {@code CommentDTO} is a Data Transfer Object (DTO) that represents a comment in the system.
 * It is used for transferring comment data between layers, such as from the controller to the service.
 * This class is annotated with validation constraints to ensure proper data integrity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO implements Serializable {

    /**
     * The unique identifier of the comment.
     */
    private Long commentId;

    /**
     * The text content of the comment.
     * <p>
     * This field is required and cannot be blank. The length of the comment text cannot exceed 1000 characters.
     * </p>
     */
    @NotBlank(message = "Text of a comment is required")
    @Size(max = 1000, message = "Comment text must not exceed 1000 characters")
    private String text;

    /**
     * The unique identifier of the user who created the comment.
     * <p>
     * This field is required and cannot be null.
     * </p>
     */
    @NotNull(message = "User Id is required")
    private Long userId;

    /**
     * The unique identifier of the task associated with the comment.
     * <p>
     * This field is required and cannot be null.
     * </p>
     */
    @NotNull(message = "Task Id is required")
    private Long taskId;

    /**
     * The version of the comment used for concurrency control.
     */
    private Long version;

    /**
     * Constructor for creating a new {@code CommentDTO} without a commentId.
     *
     * @param text    The text of the comment.
     * @param userId  The ID of the user who created the comment.
     * @param taskId  The ID of the task the comment is related to.
     * @param version The version of the comment for concurrency control.
     */
    public CommentDTO(String text, Long userId, Long taskId, Long version) {
        this.text = text;
        this.userId = userId;
        this.taskId = taskId;
        this.version = version;
    }
}