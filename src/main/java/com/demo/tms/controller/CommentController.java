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

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final Converter converter;

    @Autowired
    public CommentController(CommentService commentService, Converter converter) {
        this.commentService = commentService;
        this.converter = converter;
    }

    @PostMapping
    public ResponseEntity<CommentDTO> createComment(@Valid @RequestBody CommentDTO commentDTO) {
        Comment comment = converter.convertToComment(commentDTO);
        Comment savedComment = commentService.saveComment(comment);
        return ResponseEntity.ok(converter.convertToCommentDTO(savedComment));
    }

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

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        boolean isDeleted = commentService.deleteComment(commentId);
        if (!isDeleted) {
            throw new ResourceNotFoundException("Comment with ID " + commentId + " not found");
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDTO> getCommentById(@PathVariable Long commentId) {
        Comment comment = commentService.getCommentById(commentId);
        return comment != null ? ResponseEntity.ok(converter.convertToCommentDTO(comment)) :
                ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<PagedResponseDTO<CommentDTO>> getAllComments(Pageable pageable) {
        Page<Comment> comments = commentService.getAllComments(pageable);
        List<CommentDTO> commentDTOs = comments.getContent().stream()
                .map(converter::convertToCommentDTO)
                .toList();

        PagedResponseDTO<CommentDTO> response = createResponse(commentDTOs, comments);
        return ResponseEntity.ok(response);
    }

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