package com.demo.tms.service;

import com.demo.tms.exception.ResourceNotFoundException;
import com.demo.tms.entity.User;
import com.demo.tms.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code UserServiceImpl} is the implementation of the {@link UserService} interface.
 * <p>
 * This service handles business logic related to {@link User} entities, including operations such as
 * saving, updating, deleting, and retrieving users. It also ensures that passwords are encoded
 * before being saved to the database.
 * </p>
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a new {@code UserServiceImpl} with the specified repositories and password encoder.
     *
     * @param userRepository  the {@link UserRepository} to interact with user data
     * @param passwordEncoder the {@link PasswordEncoder} to encode user passwords
     */
    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Saves a new user. The user's password is encoded before saving.
     *
     * @param user the {@link User} entity to be saved
     * @return the saved {@link User} entity
     */
    @Override
    @Transactional
    public User saveUser(User user) {
        String password = passwordEncoder.encode(user.getPassword());
        user.setPassword(password);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    /**
     * Updates an existing user. If the user does not exist, an exception is thrown.
     * The user's password is encoded before saving.
     *
     * @param userId      the ID of the user to be updated
     * @param updatedUser the updated {@link User} entity
     * @return the updated {@link User} entity
     * @throws ResourceNotFoundException if the user with the specified ID is not found
     */
    @Override
    @Transactional
    public User updateUser(Long userId, User updatedUser) {
        try {
            User existingUser = userRepository.findById(userId).orElseThrow(() ->
                    new ResourceNotFoundException("User with ID " + userId + " not found"));

            // Update user fields
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            existingUser.setRole(updatedUser.getRole());
            existingUser.setEnabled(updatedUser.isEnabled());

            return userRepository.save(existingUser);
        } catch (Exception e) {
            log.error("Error updating user with ID {}: {}", userId, e.getMessage(), e);
            throw new ResourceNotFoundException("Error updating user with ID " + userId);
        }
    }

    /**
     * Deletes a user by its ID.
     *
     * @param userId the ID of the user to be deleted
     * @return {@code true} if the user was successfully deleted, otherwise {@code false}
     */
    @Override
    @Transactional
    public boolean deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    /**
     * Retrieves a user by its ID.
     *
     * @param userId the ID of the user to retrieve
     * @return the {@link User} entity with the specified ID
     * @throws ResourceNotFoundException if the user is not found
     */
    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Retrieves all users with pagination.
     *
     * @param pageable the pagination information
     * @return a {@link Page} of {@link User} entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}