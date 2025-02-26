package com.demo.tms.service;

import com.demo.tms.entity.Role;

import java.util.List;

/**
 * {@code RoleService} defines the contract for managing {@link Role} entities.
 * <p>
 * This interface includes methods for saving, updating, deleting, and retrieving roles. It provides
 * the necessary abstractions for working with roles in the system, ensuring that roles can be
 * managed effectively.
 * </p>
 */
public interface RoleService {

    /**
     * Saves a new role.
     *
     * @param role the {@link Role} entity to be saved
     * @return the saved {@link Role} entity
     */
    Role saveRole(Role role);

    /**
     * Updates an existing role by its ID.
     * If the role with the specified ID does not exist, an exception is thrown.
     *
     * @param roleId      the ID of the role to be updated
     * @param updatedRole the updated {@link Role} entity
     * @return the updated {@link Role} entity
     */
    Role updateRole(Long roleId, Role updatedRole);

    /**
     * Deletes a role by its ID.
     *
     * @param roleId the ID of the role to be deleted
     * @return {@code true} if the role was successfully deleted, otherwise {@code false}
     */
    boolean deleteRole(Long roleId);

    /**
     * Retrieves a role by its ID.
     *
     * @param roleId the ID of the role to retrieve
     * @return the {@link Role} entity with the specified ID
     */
    Role getRoleById(Long roleId);

    /**
     * Retrieves all roles in the system.
     *
     * @return a {@link List} of all {@link Role} entities
     */
    List<Role> getAllRoles();
}