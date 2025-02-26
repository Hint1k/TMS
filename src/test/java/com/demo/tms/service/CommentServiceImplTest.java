package com.demo.tms.service;

import com.demo.tms.entity.Comment;
import com.demo.tms.entity.User;
import com.demo.tms.entity.Task;
import com.demo.tms.exception.ResourceNotFoundException;
import com.demo.tms.exception.OptimisticLockingException;
import com.demo.tms.repository.CommentRepository;
import com.demo.tms.repository.TaskRepository;
import com.demo.tms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment comment;
    private Comment updatedComment;
    private Long commentId;
    private User user;
    private Task task;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setUsername("user1");

        task = new Task();
        task.setTaskId(1L);
        task.setName("Test Task");

        commentId = 1L;
        comment = new Comment();
        comment.setCommentId(commentId);
        comment.setText("This is a comment.");
        comment.setUser(user);
        comment.setTask(task);
        comment.setVersion(1L);

        updatedComment = new Comment();
        updatedComment.setCommentId(commentId);
        updatedComment.setText("This is an updated comment.");
        updatedComment.setUser(user);
        updatedComment.setTask(task);
        updatedComment.setVersion(2L);
    }

    @Test
    void testSaveComment_Success() {
        // Mock repository behavior
        when(userRepository.existsById(user.getUserId())).thenReturn(true);
        when(taskRepository.existsById(task.getTaskId())).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment savedComment = commentService.saveComment(comment);

        assertNotNull(savedComment);
        assertEquals(commentId, savedComment.getCommentId());
        verify(commentRepository, times(1)).save(any(Comment.class));
        verify(userRepository, times(1)).existsById(user.getUserId());
        verify(taskRepository, times(1)).existsById(task.getTaskId());
    }

    @Test
    void testUpdateComment_Success() {
        // Mock repository behavior
        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.of(comment));
        when(userRepository.existsById(user.getUserId())).thenReturn(true);
        when(taskRepository.existsById(task.getTaskId())).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(updatedComment);

        Comment updated = commentService.updateComment(commentId, updatedComment);

        assertNotNull(updated);
        assertEquals(updatedComment.getText(), updated.getText());
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testUpdateComment_OptimisticLockingException() {
        String message = "Optimistic locking failure: Comment was updated by another transaction";

        // Mock user and task behavior
        User author = new User();
        author.setUserId(1L);  // Mock the user ID
        Task task = new Task();
        task.setTaskId(1L);  // Mock the task ID

        when(userRepository.existsById(author.getUserId())).thenReturn(true);  // Mock user existence
        when(taskRepository.existsById(task.getTaskId())).thenReturn(true);  // Mock task existence

        // Mock comment repository behavior
        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenThrow(new OptimisticLockingException(message));

        // Perform the test and assert the expected exception
        OptimisticLockingException exception = assertThrows(OptimisticLockingException.class, () -> {
            commentService.updateComment(commentId, updatedComment);
        });

        assertTrue(exception.getMessage().contains(message));
    }

    @Test
    void testDeleteComment_Success() {
        // Mock repository behavior
        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.of(comment));

        boolean isDeleted = commentService.deleteComment(commentId);

        assertTrue(isDeleted);
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).deleteById(commentId);
    }

    @Test
    void testDeleteComment_CommentNotFound() {
        // Mock repository behavior
        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.empty());

        boolean isDeleted = commentService.deleteComment(commentId);

        assertFalse(isDeleted);
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(0)).deleteById(commentId);
    }

    @Test
    void testGetCommentById_Success() {
        // Mock repository behavior
        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.of(comment));

        Comment foundComment = commentService.getCommentById(commentId);

        assertNotNull(foundComment);
        assertEquals(commentId, foundComment.getCommentId());
        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    void testGetCommentById_CommentNotFound() {
        // Mock repository behavior
        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            commentService.getCommentById(commentId);
        });

        assertEquals("Comment not found", exception.getMessage());
    }

    @Test
    void testGetAllComments() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Comment> commentPage = mock(Page.class);

        when(commentRepository.findAll(pageable)).thenReturn(commentPage);

        Page<Comment> comments = commentService.getAllComments(pageable);

        assertNotNull(comments);
        verify(commentRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetCommentsByTask() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Comment> commentPage = mock(Page.class);
        when(commentRepository.findByTaskId(anyLong(), eq(pageable))).thenReturn(commentPage);

        Page<Comment> comments = commentService.getCommentsByTask(1L, pageable);

        assertNotNull(comments);
        verify(commentRepository, times(1)).findByTaskId(anyLong(), eq(pageable));
    }

    @Test
    void testGetCommentsByUser() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Comment> commentPage = mock(Page.class);
        when(commentRepository.findByUserId(anyLong(), eq(pageable))).thenReturn(commentPage);

        Page<Comment> comments = commentService.getCommentsByUser(1L, pageable);

        assertNotNull(comments);
        verify(commentRepository, times(1)).findByUserId(anyLong(), eq(pageable));
    }
}