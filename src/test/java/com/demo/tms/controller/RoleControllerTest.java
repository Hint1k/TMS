package com.demo.tms.controller;

import com.demo.tms.dto.RoleDTO;
import com.demo.tms.entity.Role;
import com.demo.tms.exception.ResourceNotFoundException;
import com.demo.tms.service.RoleService;
import com.demo.tms.converter.Converter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock
    private RoleService roleService;

    @Mock
    private Converter converter;

    @InjectMocks
    private RoleController roleController;

    private Role role;
    private RoleDTO roleDTO;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setRoleId(1L);
        role.setAuthority("ADMIN");

        roleDTO = new RoleDTO();
        roleDTO.setRoleId(1L);
        roleDTO.setAuthority("ADMIN");
    }

    @Test
    void createRole_ShouldReturnRoleDTO() {
        when(converter.convertToRole(roleDTO)).thenReturn(role);
        when(roleService.saveRole(role)).thenReturn(role);
        when(converter.convertToRoleDTO(role)).thenReturn(roleDTO);

        ResponseEntity<RoleDTO> response = roleController.createRole(roleDTO);

        assertNotNull(response);
        assertEquals(roleDTO, response.getBody());
        verify(roleService, times(1)).saveRole(role);
    }

    @Test
    void updateRole_ShouldReturnUpdatedRoleDTO() {
        when(converter.convertToRole(roleDTO)).thenReturn(role);
        when(roleService.updateRole(1L, role)).thenReturn(role);
        when(converter.convertToRoleDTO(role)).thenReturn(roleDTO);

        ResponseEntity<RoleDTO> response = roleController.updateRole(1L, roleDTO);

        assertNotNull(response);
        assertEquals(roleDTO, response.getBody());
        verify(roleService, times(1)).updateRole(1L, role);
    }

    @Test
    void updateRole_ShouldThrowResourceNotFoundException_WhenRoleNotFound() {
        when(converter.convertToRole(roleDTO)).thenReturn(role);
        when(roleService.updateRole(1L, role)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> roleController.updateRole(1L, roleDTO));
    }

    @Test
    void deleteRole_ShouldReturnNoContent_WhenRoleDeleted() {
        when(roleService.deleteRole(1L)).thenReturn(true);

        ResponseEntity<Void> response = roleController.deleteRole(1L);

        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        verify(roleService, times(1)).deleteRole(1L);
    }

    @Test
    void deleteRole_ShouldThrowResourceNotFoundException_WhenRoleNotFound() {
        when(roleService.deleteRole(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> roleController.deleteRole(1L));
    }

    @Test
    void getRoleById_ShouldReturnRoleDTO_WhenRoleExists() {
        when(roleService.getRoleById(1L)).thenReturn(role);
        when(converter.convertToRoleDTO(role)).thenReturn(roleDTO);

        ResponseEntity<RoleDTO> response = roleController.getRoleById(1L);

        assertNotNull(response);
        assertEquals(roleDTO, response.getBody());
        verify(roleService, times(1)).getRoleById(1L);
    }

    @Test
    void getRoleById_ShouldReturnNotFound_WhenRoleDoesNotExist() {
        when(roleService.getRoleById(1L)).thenReturn(null);

        ResponseEntity<RoleDTO> response = roleController.getRoleById(1L);

        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void getAllRoles_ShouldReturnListOfRoleDTOs() {
        List<Role> roles = List.of(role);
        List<RoleDTO> roleDTOs = List.of(roleDTO);

        when(roleService.getAllRoles()).thenReturn(roles);
        when(converter.convertToRoleDTO(role)).thenReturn(roleDTO);

        ResponseEntity<List<RoleDTO>> response = roleController.getAllRoles();

        assertNotNull(response);
        assertEquals(roleDTOs, response.getBody());
        verify(roleService, times(1)).getAllRoles();
    }
}