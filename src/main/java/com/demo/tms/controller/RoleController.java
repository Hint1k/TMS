package com.demo.tms.controller;

import com.demo.tms.dto.RoleDTO;
import com.demo.tms.entity.Role;
import com.demo.tms.exception.ResourceNotFoundException;
import com.demo.tms.service.RoleService;
import com.demo.tms.converter.Converter;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The {@code RoleController} class handles HTTP requests related to roles.
 * It provides RESTful endpoints for creating, updating, deleting, and retrieving roles.
 * The class utilizes the {@link RoleService} to perform operations on roles and
 * {@link Converter} to convert between DTOs and entity objects.
 */
@RestController
@RequestMapping("/api/roles")
@Slf4j
public class RoleController {

    private final RoleService roleService;
    private final Converter converter;

    /**
     * Constructs a new {@code RoleController} with the specified dependencies.
     *
     * @param roleService The service responsible for managing role data.
     * @param converter   The converter used to transform between {@link RoleDTO} and {@link Role} entities.
     */
    @Autowired
    public RoleController(RoleService roleService, Converter converter) {
        this.roleService = roleService;
        this.converter = converter;
    }

    /**
     * Creates a new role.
     * <p>
     * The method accepts a {@link RoleDTO} object, converts it to a {@link Role} entity,
     * saves it through the {@code roleService}, and returns the saved role as a {@link RoleDTO}.
     * </p>
     *
     * @param roleDTO The {@link RoleDTO} object containing the role data to be saved.
     * @return A {@link ResponseEntity} containing the saved role as a {@link RoleDTO}.
     */
    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        Role role = converter.convertToRole(roleDTO);
        Role savedRole = roleService.saveRole(role);
        return ResponseEntity.ok(converter.convertToRoleDTO(savedRole));
    }

    /**
     * Updates an existing role.
     * <p>
     * The method accepts a {@link RoleDTO} object with updated data, converts it to a {@link Role} entity,
     * and updates the role with the given {@code roleId}. If the role is not found,
     * a {@link ResourceNotFoundException} is thrown.
     * </p>
     *
     * @param roleId   The ID of the role to be updated.
     * @param roleDTO  The {@link RoleDTO} object containing the updated role data.
     * @return A {@link ResponseEntity} containing the updated role as a {@link RoleDTO}.
     * @throws ResourceNotFoundException If the role with the given {@code roleId} is not found.
     */
    @PutMapping("/{roleId}")
    public ResponseEntity<RoleDTO> updateRole(@Valid @PathVariable Long roleId, @RequestBody RoleDTO roleDTO) {
        Role updatedRole = converter.convertToRole(roleDTO);
        updatedRole.setRoleId(roleId);
        Role role = roleService.updateRole(roleId, updatedRole);
        if (role == null) {
            throw new ResourceNotFoundException("Role with ID " + roleId + " not found");
        }
        return ResponseEntity.ok(converter.convertToRoleDTO(role));
    }

    /**
     * Deletes a role by its ID.
     * <p>
     * The method deletes the role associated with the provided {@code roleId}.
     * If the role is not found, a {@link ResourceNotFoundException} is thrown.
     * </p>
     *
     * @param roleId The ID of the role to be deleted.
     * @return A {@link ResponseEntity} indicating the result of the delete operation.
     * @throws ResourceNotFoundException If the role with the given {@code roleId} is not found.
     */
    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        boolean isDeleted = roleService.deleteRole(roleId);
        if (!isDeleted) {
            throw new ResourceNotFoundException("Role with ID " + roleId + " not found");
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves a role by its ID.
     * <p>
     * The method fetches the role with the given {@code roleId}. If the role is found,
     * it returns the role as a {@link RoleDTO}. If not, it returns a {@code 404 Not Found} response.
     * </p>
     *
     * @param roleId The ID of the role to be retrieved.
     * @return A {@link ResponseEntity} containing the role as a {@link RoleDTO} or a {@code 404 Not Found} response.
     */
    @GetMapping("/{roleId}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long roleId) {
        Role role = roleService.getRoleById(roleId);
        return role != null ? ResponseEntity.ok(converter.convertToRoleDTO(role)) : ResponseEntity.notFound().build();
    }

    /**
     * Retrieves all roles.
     * <p>
     * The method returns a list of all roles, with each role represented as a {@link RoleDTO}.
     * </p>
     *
     * @return A {@link ResponseEntity} containing a list of {@link RoleDTO} objects representing all roles.
     */
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        List<RoleDTO> roleDTOs = roles.stream()
                .map(converter::convertToRoleDTO)
                .toList();
        return ResponseEntity.ok(roleDTOs);
    }
}