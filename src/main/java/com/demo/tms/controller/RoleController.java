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

@RestController
@RequestMapping("/api/roles")
@Slf4j
public class RoleController {

    private final RoleService roleService;
    private final Converter converter;

    @Autowired
    public RoleController(RoleService roleService, Converter converter) {
        this.roleService = roleService;
        this.converter = converter;
    }

    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        Role role = converter.convertToRole(roleDTO);
        Role savedRole = roleService.saveRole(role);
        return ResponseEntity.ok(converter.convertToRoleDTO(savedRole));
    }

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

    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        boolean isDeleted = roleService.deleteRole(roleId);
        if (!isDeleted) {
            throw new ResourceNotFoundException("Role with ID " + roleId + " not found");
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long roleId) {
        Role role = roleService.getRoleById(roleId);
        return role != null ? ResponseEntity.ok(converter.convertToRoleDTO(role)) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        List<RoleDTO> roleDTOs = roles.stream()
                .map(converter::convertToRoleDTO)
                .toList();
        return ResponseEntity.ok(roleDTOs);
    }
}