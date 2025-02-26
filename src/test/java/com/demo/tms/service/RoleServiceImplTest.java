package com.demo.tms.service;

import com.demo.tms.entity.Role;
import com.demo.tms.entity.User;
import com.demo.tms.exception.ResourceNotFoundException;
import com.demo.tms.repository.RoleRepository;
import com.demo.tms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role role;
    private User user;

    @BeforeEach
    void setUp() {
        // Creating a mock user
        user = new User();
        user.setUserId(1L);

        // Creating a mock role
        role = new Role();
        role.setRoleId(1L);
        role.setAuthority("ADMIN");
        role.setUser(user);
    }

    @Test
    void testSaveRole_WhenUserExists_ShouldSaveRole() {
        // Given - The user exists
        when(userRepository.existsById(user.getUserId())).thenReturn(true);
        when(roleRepository.save(role)).thenReturn(role);

        // When - Saving the role
        Role savedRole = roleService.saveRole(role);

        // Then - Role should be saved successfully
        assertNotNull(savedRole);
        assertEquals("ADMIN", savedRole.getAuthority());
        verify(roleRepository).save(role);
    }

    @Test
    void testSaveRole_WhenUserDoesNotExist_ShouldThrowException() {
        // Given - The user does not exist
        when(userRepository.existsById(user.getUserId())).thenReturn(false);

        // When & Then - Expecting exception
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> roleService.saveRole(role));

        assertEquals("User with ID 1 not found", exception.getMessage());
        verify(roleRepository, never()).save(any());
    }

    @Test
    void testUpdateRole_WhenRoleExists_ShouldUpdateRole() {
        // Given - Role exists in DB
        Role updatedRole = new Role();
        updatedRole.setAuthority("USER");
        updatedRole.setUser(user);

        when(roleRepository.findById(role.getRoleId())).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenReturn(updatedRole);

        // When - Updating role
        Role result = roleService.updateRole(role.getRoleId(), updatedRole);

        // Then - Verify role is updated
        assertNotNull(result);
        assertEquals("USER", result.getAuthority());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void testUpdateRole_WhenRoleDoesNotExist_ShouldThrowException() {
        // Given - Role does not exist
        when(roleRepository.findById(role.getRoleId())).thenReturn(Optional.empty());

        // When & Then - Expecting exception
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> roleService.updateRole(role.getRoleId(), role));

        assertEquals("Error updating role with ID " + role.getRoleId(), exception.getMessage());
        verify(roleRepository, never()).save(any());
    }

    @Test
    void testDeleteRole_WhenRoleExists_ShouldDeleteRole() {
        // Given - Role exists
        when(roleRepository.findById(role.getRoleId())).thenReturn(Optional.of(role));
        doNothing().when(roleRepository).deleteById(role.getRoleId());

        // When - Deleting role
        boolean isDeleted = roleService.deleteRole(role.getRoleId());

        // Then - Role should be deleted
        assertTrue(isDeleted);
        verify(roleRepository).deleteById(role.getRoleId());
    }

    @Test
    void testDeleteRole_WhenRoleDoesNotExist_ShouldReturnFalse() {
        // Given - Role does not exist
        when(roleRepository.findById(role.getRoleId())).thenReturn(Optional.empty());

        // When - Trying to delete
        boolean isDeleted = roleService.deleteRole(role.getRoleId());

        // Then - Should return false
        assertFalse(isDeleted);
        verify(roleRepository, never()).deleteById(any());
    }

    @Test
    void testGetRoleById_WhenRoleExists_ShouldReturnRole() {
        // Given - Role exists
        when(roleRepository.findById(role.getRoleId())).thenReturn(Optional.of(role));

        // When - Retrieving role
        Role retrievedRole = roleService.getRoleById(role.getRoleId());

        // Then - Should return role
        assertNotNull(retrievedRole);
        assertEquals("ADMIN", retrievedRole.getAuthority());
    }

    @Test
    void testGetRoleById_WhenRoleDoesNotExist_ShouldThrowException() {
        // Given - Role does not exist
        when(roleRepository.findById(role.getRoleId())).thenReturn(Optional.empty());

        // When & Then - Expecting exception
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> roleService.getRoleById(role.getRoleId()));

        assertEquals("Role not found", exception.getMessage());
    }

    @Test
    void testGetAllRoles_ShouldReturnListOfRoles() {
        // Given - Some roles exist
        List<Role> roles = List.of(role);
        when(roleRepository.findAll()).thenReturn(roles);

        // When - Retrieving all roles
        List<Role> result = roleService.getAllRoles();

        // Then - Should return list of roles
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ADMIN", result.getFirst().getAuthority());
    }
}