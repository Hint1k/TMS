package com.demo.tms.controller;

import com.demo.tms.converter.Converter;
import com.demo.tms.dto.PagedResponseDTO;
import com.demo.tms.dto.UserDTO;
import com.demo.tms.entity.User;
import com.demo.tms.exception.ResourceNotFoundException;
import com.demo.tms.service.UserService;
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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Converter converter;

    @InjectMocks
    private UserController userController;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        userDTO = new UserDTO();
        userDTO.setUserId(1L);
        userDTO.setUsername("testuser");
        userDTO.setEmail("test@example.com");
    }

    @Test
    void createUser_ShouldReturnUserDTO() {
        when(converter.convertToUser(userDTO)).thenReturn(user);
        when(userService.saveUser(user)).thenReturn(user);
        when(converter.convertToUserDTO(user)).thenReturn(userDTO);

        ResponseEntity<UserDTO> response = userController.createUser(userDTO);

        assertNotNull(response);
        assertEquals(userDTO, response.getBody());
        verify(userService, times(1)).saveUser(user);
    }

    @Test
    void updateUser_ShouldReturnUpdatedUserDTO() {
        when(converter.convertToUser(userDTO)).thenReturn(user);
        when(userService.updateUser(1L, user)).thenReturn(user);
        when(converter.convertToUserDTO(user)).thenReturn(userDTO);

        ResponseEntity<UserDTO> response = userController.updateUser(1L, userDTO);

        assertNotNull(response);
        assertEquals(userDTO, response.getBody());
        verify(userService, times(1)).updateUser(1L, user);
    }

    @Test
    void updateUser_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        when(converter.convertToUser(userDTO)).thenReturn(user);
        when(userService.updateUser(1L, user)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> userController.updateUser(1L, userDTO));
    }

    @Test
    void deleteUser_ShouldReturnNoContent_WhenUserDeleted() {
        when(userService.deleteUser(1L)).thenReturn(true);

        ResponseEntity<Void> response = userController.deleteUser(1L);

        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void deleteUser_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        when(userService.deleteUser(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userController.deleteUser(1L));
    }

    @Test
    void getUserById_ShouldReturnUserDTO_WhenUserExists() {
        when(userService.getUserById(1L)).thenReturn(user);
        when(converter.convertToUserDTO(user)).thenReturn(userDTO);

        ResponseEntity<UserDTO> response = userController.getUserById(1L);

        assertNotNull(response);
        assertEquals(userDTO, response.getBody());
        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserDoesNotExist() {
        when(userService.getUserById(1L)).thenReturn(null);

        ResponseEntity<UserDTO> response = userController.getUserById(1L);

        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void getAllUsers_ShouldReturnPagedResponse() {
        Page<User> userPage = new PageImpl<>(List.of(user));
        PagedResponseDTO<UserDTO> pagedResponseDTO =
                new PagedResponseDTO<>(List.of(userDTO), 0, 10, 1, 1);

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);
        when(converter.convertToUserDTO(user)).thenReturn(userDTO);

        ResponseEntity<PagedResponseDTO<UserDTO>> response = userController.getAllUsers(Pageable.unpaged());

        assertNotNull(response);
        assertEquals(pagedResponseDTO.getContent(), Objects.requireNonNull(response.getBody()).getContent());
        verify(userService, times(1)).getAllUsers(any(Pageable.class));
    }
}