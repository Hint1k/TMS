package com.demo.tms.controller;

import com.demo.tms.converter.Converter;
import com.demo.tms.dto.CommentDTO;
import com.demo.tms.dto.PagedResponseDTO;
import com.demo.tms.entity.Comment;
import com.demo.tms.exception.ResourceNotFoundException;
import com.demo.tms.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @Mock
    private Converter converter;

    @InjectMocks
    private CommentController commentController;

    private Comment comment;
    private CommentDTO commentDTO;

    @BeforeEach
    void setUp() {
        comment = new Comment();
        comment.setCommentId(1L);
        comment.setText("Test comment");

        commentDTO = new CommentDTO();
        commentDTO.setCommentId(1L);
        commentDTO.setText("Test comment");
    }

    @Test
    void createComment_ShouldReturnCommentDTO() {
        when(converter.convertToComment(commentDTO)).thenReturn(comment);
        when(commentService.saveComment(comment)).thenReturn(comment);
        when(converter.convertToCommentDTO(comment)).thenReturn(commentDTO);

        ResponseEntity<CommentDTO> response = commentController.createComment(commentDTO);

        assertNotNull(response);
        assertEquals(commentDTO, response.getBody());
        verify(commentService, times(1)).saveComment(comment);
    }

    @Test
    void updateComment_ShouldReturnUpdatedCommentDTO() {
        when(converter.convertToComment(commentDTO)).thenReturn(comment);
        when(commentService.updateComment(1L, comment)).thenReturn(comment);
        when(converter.convertToCommentDTO(comment)).thenReturn(commentDTO);

        ResponseEntity<CommentDTO> response = commentController.updateComment(1L, commentDTO);

        assertNotNull(response);
        assertEquals(commentDTO, response.getBody());
        verify(commentService, times(1)).updateComment(1L, comment);
    }

    @Test
    void updateComment_ShouldThrowResourceNotFoundException_WhenCommentNotFound() {
        when(converter.convertToComment(commentDTO)).thenReturn(comment);
        when(commentService.updateComment(1L, comment)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> commentController.updateComment(1L, commentDTO));
    }

    @Test
    void deleteComment_ShouldReturnNoContent_WhenCommentDeleted() {
        when(commentService.deleteComment(1L)).thenReturn(true);

        ResponseEntity<Void> response = commentController.deleteComment(1L);

        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        verify(commentService, times(1)).deleteComment(1L);
    }

    @Test
    void deleteComment_ShouldThrowResourceNotFoundException_WhenCommentNotFound() {
        when(commentService.deleteComment(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> commentController.deleteComment(1L));
    }

    @Test
    void getCommentById_ShouldReturnCommentDTO_WhenCommentExists() {
        when(commentService.getCommentById(1L)).thenReturn(comment);
        when(converter.convertToCommentDTO(comment)).thenReturn(commentDTO);

        ResponseEntity<CommentDTO> response = commentController.getCommentById(1L);

        assertNotNull(response);
        assertEquals(commentDTO, response.getBody());
        verify(commentService, times(1)).getCommentById(1L);
    }

    @Test
    void getCommentById_ShouldReturnNotFound_WhenCommentDoesNotExist() {
        when(commentService.getCommentById(1L)).thenReturn(null);

        ResponseEntity<CommentDTO> response = commentController.getCommentById(1L);

        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void getAllComments_ShouldReturnPagedResponse() {
        Page<Comment> commentPage = new PageImpl<>(List.of(comment));
        PagedResponseDTO<CommentDTO> pagedResponseDTO =
                new PagedResponseDTO<>(List.of(commentDTO), 0, 10, 1, 1);

        when(commentService.getAllComments(any(Pageable.class))).thenReturn(commentPage);
        when(converter.convertToCommentDTO(comment)).thenReturn(commentDTO);

        ResponseEntity<PagedResponseDTO<CommentDTO>> response = commentController.getAllComments(Pageable.unpaged());

        assertNotNull(response);
        assertEquals(pagedResponseDTO.getContent(), response.getBody().getContent());
        verify(commentService, times(1)).getAllComments(any(Pageable.class));
    }
}