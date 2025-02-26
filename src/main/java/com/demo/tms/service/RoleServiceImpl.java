package com.demo.tms.service;

import com.demo.tms.entity.Role;
import com.demo.tms.exception.ResourceNotFoundException;
import com.demo.tms.repository.RoleRepository;
import com.demo.tms.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@code RoleServiceImpl} is the implementation of the {@link RoleService} interface.
 * <p>
 * This service handles the business logic related to {@link Role} entities, including operations such as
 * saving, updating, deleting, and retrieving roles. It also ensures that the user associated with a role exists
 * before performing any operation.
 * </p>
 */
@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    /**
     * Constructs a new {@code RoleServiceImpl} with the specified repositories.
     *
     * @param roleRepository the {@link RoleRepository} to interact with role data
     * @param userRepository the {@link UserRepository} to interact with user data
     */
    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    /**
     * Saves a new role after validating that the user associated with the role exists.
     *
     * @param role the {@link Role} entity to be saved
     * @return the saved {@link Role} entity
     */
    @Override
    @Transactional
    public Role saveRole(Role role) {
        validateUser(role);
        return roleRepository.save(role);
    }

    /**
     * Updates an existing role. If the role does not exist, an exception is thrown.
     *
     * @param roleId      the ID of the role to be updated
     * @param updatedRole the updated {@link Role} entity
     * @return the updated {@link Role} entity
     * @throws ResourceNotFoundException if the role with the specified ID is not found
     */
    @Override
    @Transactional
    public Role updateRole(Long roleId, Role updatedRole) {
        try {
            Role existingRole = roleRepository.findById(roleId).orElseThrow(() ->
                    new ResourceNotFoundException("Role with ID " + roleId + " not found"));

            // Update role fields
            existingRole.setAuthority(updatedRole.getAuthority());
            existingRole.setUser(updatedRole.getUser());

            return roleRepository.save(existingRole);
        } catch (Exception e) {
            log.error("Error updating role with ID {}: {}", roleId, e.getMessage(), e);
            throw new ResourceNotFoundException("Error updating role with ID " + roleId);
        }
    }

    /**
     * Deletes a role by its ID.
     *
     * @param roleId the ID of the role to be deleted
     * @return {@code true} if the role was successfully deleted, otherwise {@code false}
     */
    @Override
    @Transactional
    public boolean deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role != null) {
            roleRepository.deleteById(roleId);
            return true;
        }
        return false;
    }

    /**
     * Retrieves a role by its ID.
     *
     * @param roleId the ID of the role to retrieve
     * @return the {@link Role} entity with the specified ID
     * @throws ResourceNotFoundException if the role is not found
     */
    @Override
    @Transactional(readOnly = true)
    public Role getRoleById(Long roleId) {
        return roleRepository.findById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role not found"));
    }

    /**
     * Retrieves all roles.
     *
     * @return a list of all {@link Role} entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    /**
     * Validates that the user associated with the role exists in the system.
     *
     * @param role the {@link Role} entity to validate
     * @throws ResourceNotFoundException if the user associated with the role does not exist
     */
    private void validateUser(Role role) {
        if (role.getUser() != null && !userRepository.existsById(role.getUser().getUserId())) {
            throw new ResourceNotFoundException("User with ID " + role.getUser().getUserId() + " not found");
        }
    }
}