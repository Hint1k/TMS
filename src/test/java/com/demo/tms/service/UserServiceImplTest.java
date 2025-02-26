package com.demo.tms.service;
import com.demo.tms.entity.User;
import com.demo.tms.exception.ResourceNotFoundException;
import com.demo.tms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private User updatedUser;

    @BeforeEach
    void setUp() {
        user = new User("john_doe", "john@example.com", "password123",
                null, null, null, true);
        updatedUser = new User(1L,"john_doe_updated", "john_updated@example.com",
                "password456", null, null, null, true);
    }

    // Test saveUser
    @Test
    void testSaveUser() {
        // Arrange
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User savedUser = userService.saveUser(user);

        // Assert
        assertNotNull(savedUser);
        assertEquals("encoded_password", savedUser.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    // Test updateUser - User exists
    @Test
    void testUpdateUser_Success() {
        Long userId = 1L;

        // Arrange
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.encode(updatedUser.getPassword())).thenReturn("password456");
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        User updated = userService.updateUser(userId, updatedUser);

        // Assert
        assertNotNull(updated);
        assertEquals(updatedUser.getUsername(), updated.getUsername());
        assertEquals(updatedUser.getEmail(), updated.getEmail());
        assertEquals("password456", updated.getPassword());
        verify(userRepository, times(1)).findById(userId);
        verify(passwordEncoder, times(1)).encode(updatedUser.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    // Test updateUser - User not found
    @Test
    void testUpdateUser_UserNotFound() {
        Long userId = 1L;

        // Arrange
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userId, updatedUser);
        });
        System.out.println("Actual Exception Message: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("Error updating user with ID " + userId));
    }

    // Test deleteUser - User exists
    @Test
    void testDeleteUser_Success() {
        Long userId = 1L;

        // Arrange
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));

        // Act
        boolean result = userService.deleteUser(userId);

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).deleteById(userId);
    }

    // Test deleteUser - User not found
    @Test
    void testDeleteUser_UserNotFound() {
        Long userId = 1L;

        // Arrange
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        // Act
        boolean result = userService.deleteUser(userId);

        // Assert
        assertFalse(result);
        verify(userRepository, times(0)).deleteById(userId);
    }

    // Test getUserById - User exists
    @Test
    void testGetUserById_Success() {
        Long userId = 1L;

        // Arrange
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(updatedUser));

        // Act
        User foundUser = userService.getUserById(userId);

        // Assert
        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getUserId());
        verify(userRepository, times(1)).findById(userId);
    }

    // Test getUserById - User not found
    @Test
    void testGetUserById_UserNotFound() {
        Long userId = 1L;

        // Arrange
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(userId);
        });
        assertTrue(exception.getMessage().contains("User not found"));
    }

    // Test getAllUsers
    @Test
    void testGetAllUsers() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Page<User> userPage = mock(Page.class);
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // Act
        Page<User> users = userService.getAllUsers(pageable);

        // Assert
        assertNotNull(users);
        verify(userRepository, times(1)).findAll(pageable);
    }
}