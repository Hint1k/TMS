package com.demo.tms.controller;

import com.demo.tms.converter.Converter;
import com.demo.tms.dto.CommentDTO;
import com.demo.tms.dto.PagedResponseDTO;
import com.demo.tms.entity.Comment;
import com.demo.tms.exception.ResourceNotFoundException;
import com.demo.tms.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The {@code CommentController} class handles HTTP requests related to comments.
 * It provides RESTful endpoints for creating, updating, deleting, and retrieving comments.
 * The class utilizes the {@link CommentService} to perform operations on comments and
 * {@link Converter} to convert between DTOs and entity objects.
 */
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final Converter converter;

    /**
     * Constructs a new {@code CommentController} with the specified dependencies.
     *
     * @param commentService The service responsible for managing comment data.
     * @param converter      The converter used to transform between {@link CommentDTO} and {@link Comment} entities.
     */
    @Autowired
    public CommentController(CommentService commentService, Converter converter) {
        this.commentService = commentService;
        this.converter = converter;
    }

    /**
     * Creates a new comment.
     * <p>
     * The method accepts a {@link CommentDTO} object, converts it to a {@link Comment} entity,
     * saves it through the {@code commentService}, and returns the saved comment as a {@link CommentDTO}.
     * </p>
     *
     * @param commentDTO The {@link CommentDTO} object containing the comment data to be saved.
     * @return A {@link ResponseEntity} containing the saved comment as a {@link CommentDTO}.
     */
    @PostMapping
    public ResponseEntity<CommentDTO> createComment(@Valid @RequestBody CommentDTO commentDTO) {
        Comment comment = converter.convertToComment(commentDTO);
        Comment savedComment = commentService.saveComment(comment);
        return ResponseEntity.ok(converter.convertToCommentDTO(savedComment));
    }

    /**
     * Updates an existing comment.
     * <p>
     * The method accepts a {@link CommentDTO} object with updated data, converts it to a {@link Comment} entity,
     * and updates the comment with the given {@code commentId}. If the comment is not found,
     * a {@link ResourceNotFoundException} is thrown.
     * </p>
     *
     * @param commentId The ID of the comment to be updated.
     * @param commentDTO The {@link CommentDTO} object containing the updated comment data.
     * @return A {@link ResponseEntity} containing the updated comment as a {@link CommentDTO}.
     * @throws ResourceNotFoundException If the comment with the given {@code commentId} is not found.
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(@Valid @PathVariable Long commentId,
                                                    @RequestBody CommentDTO commentDTO) {
        Comment updatedComment = converter.convertToComment(commentDTO);
        updatedComment.setCommentId(commentId);
        Comment comment = commentService.updateComment(commentId, updatedComment);
        if (comment == null) {
            throw new ResourceNotFoundException("Task with ID " + commentId + " not found");
        }
        return ResponseEntity.ok(converter.convertToCommentDTO(comment));
    }

    /**
     * Deletes a comment by its ID.
     * <p>
     * The method deletes the comment associated with the provided {@code commentId}.
     * If the comment is not found, a {@link ResourceNotFoundException} is thrown.
     * </p>
     *
     * @param commentId The ID of the comment to be deleted.
     * @return A {@link ResponseEntity} indicating the result of the delete operation.
     * @throws ResourceNotFoundException If the comment with the given {@code commentId} is not found.
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        boolean isDeleted = commentService.deleteComment(commentId);
        if (!isDeleted) {
            throw new ResourceNotFoundException("Comment with ID " + commentId + " not found");
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves a comment by its ID.
     * <p>
     * The method fetches the comment with the given {@code commentId}. If the comment is found,
     * it returns the comment as a {@link CommentDTO}. If not, it returns a {@code 404 Not Found} response.
     * </p>
     *
     * @param commentId The ID of the comment to be retrieved.
     * @return A {@link ResponseEntity} containing the comment as a {@link CommentDTO} or a {@code 404 Not Found} response.
     */
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDTO> getCommentById(@PathVariable Long commentId) {
        Comment comment = commentService.getCommentById(commentId);
        return comment != null ? ResponseEntity.ok(converter.convertToCommentDTO(comment)) :
                ResponseEntity.notFound().build();
    }

    /**
     * Retrieves all comments, with pagination support.
     * <p>
     * The method returns a paginated list of comments, with each comment represented as a {@link CommentDTO}.
     * </p>
     *
     * @param pageable The pagination information (page number, page size, etc.).
     * @return A {@link ResponseEntity} containing a {@link PagedResponseDTO} with the list of comments.
     */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<CommentDTO>> getAllComments(Pageable pageable) {
        Page<Comment> comments = commentService.getAllComments(pageable);
        List<CommentDTO> commentDTOs = comments.getContent().stream()
                .map(converter::convertToCommentDTO)
                .toList();

        PagedResponseDTO<CommentDTO> response = createResponse(commentDTOs, comments);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all comments associated with a specific task, with pagination support.
     * <p>
     * The method returns a paginated list of comments related to the task with the given {@code taskId}.
     * </p>
     *
     * @param taskId  The ID of the task for which to retrieve comments.
     * @param pageable The pagination information (page number, page size, etc.).
     * @return A {@link ResponseEntity} containing a {@link PagedResponseDTO} with the list of comments for the task.
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<PagedResponseDTO<CommentDTO>> getCommentsByTask(@PathVariable Long taskId,
                                                                          Pageable pageable) {
        Page<Comment> comments = commentService.getCommentsByTask(taskId, pageable);
        List<CommentDTO> commentDTOs = comments.getContent().stream()
                .map(converter::convertToCommentDTO)
                .toList();

        PagedResponseDTO<CommentDTO> response = createResponse(commentDTOs, comments);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all comments associated with a specific user, with pagination support.
     * <p>
     * The method returns a paginated list of comments made by the user with the given {@code userId}.
     * </p>
     *
     * @param userId  The ID of the user for whom to retrieve comments.
     * @param pageable The pagination information (page number, page size, etc.).
     * @return A {@link ResponseEntity} containing a {@link PagedResponseDTO} with the list of comments by the user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<PagedResponseDTO<CommentDTO>> getCommentsByUser(@PathVariable Long userId,
                                                                          Pageable pageable) {
        Page<Comment> comments = commentService.getCommentsByUser(userId, pageable);
        List<CommentDTO> commentDTOs = comments.getContent().stream()
                .map(converter::convertToCommentDTO)
                .toList();

        PagedResponseDTO<CommentDTO> response = createResponse(commentDTOs, comments);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a paginated response containing a list of comment DTOs and pagination metadata.
     *
     * @param commentDTOs The list of {@link CommentDTO} objects to include in the response.
     * @param comments    The {@link Page} object containing the comments and pagination data.
     * @return A {@link PagedResponseDTO} containing the comment DTOs and pagination information.
     */
    private PagedResponseDTO<CommentDTO> createResponse(List<CommentDTO> commentDTOs, Page<Comment> comments) {
        return new PagedResponseDTO<>(
                commentDTOs,
                comments.getNumber(),
                comments.getSize(),
                comments.getTotalElements(),
                comments.getTotalPages()
        );
    }
}