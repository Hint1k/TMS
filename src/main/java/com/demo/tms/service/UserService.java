package com.demo.tms.service;

import com.demo.tms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * {@code UserService} defines the contract for managing {@link User} entities.
 * <p>
 * This interface provides methods for creating, updating, deleting, and retrieving users,
 * as well as managing user-related operations in the system. It supports user management
 * by their IDs and allows retrieving users in a paginated manner.
 * </p>
 */
public interface UserService {

    /**
     * Saves a new user.
     *
     * @param user the {@link User} entity to be saved
     * @return the saved {@link User} entity
     */
    User saveUser(User user);

    /**
     * Updates an existing user by its ID.
     * If the user with the specified ID does not exist, an exception is thrown.
     *
     * @param userId      the ID of the user to be updated
     * @param updatedUser the updated {@link User} entity
     * @return the updated {@link User} entity
     */
    User updateUser(Long userId, User updatedUser);

    /**
     * Deletes a user by its ID.
     *
     * @param userId the ID of the user to be deleted
     * @return {@code true} if the user was successfully deleted, otherwise {@code false}
     */
    boolean deleteUser(Long userId);

    /**
     * Retrieves a user by its ID.
     *
     * @param userId the ID of the user to retrieve
     * @return the {@link User} entity with the specified ID
     */
    User getUserById(Long userId);

    /**
     * Retrieves all users in the system.
     *
     * @param pageable the pagination information
     * @return a {@link Page} of all {@link User} entities
     */
    Page<User> getAllUsers(Pageable pageable);
}