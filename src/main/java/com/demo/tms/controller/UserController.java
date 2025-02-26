package com.demo.tms.controller;

import com.demo.tms.dto.PagedResponseDTO;
import com.demo.tms.dto.UserDTO;
import com.demo.tms.entity.User;
import com.demo.tms.exception.ResourceNotFoundException;
import com.demo.tms.service.UserService;
import com.demo.tms.converter.Converter;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The {@code UserController} class handles HTTP requests related to users.
 * It provides RESTful endpoints for creating, updating, deleting, and retrieving user information.
 * The class uses {@link UserService} for user-related operations and {@link Converter} to convert
 * between {@link UserDTO} and {@link User} entities.
 */
@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final Converter converter;

    /**
     * Constructs a new {@code UserController} with the specified dependencies.
     *
     * @param userService The service responsible for managing user data.
     * @param converter   The converter used to transform between {@link UserDTO} and {@link User} entities.
     */
    @Autowired
    public UserController(UserService userService, Converter converter) {
        this.userService = userService;
        this.converter = converter;
    }

    /**
     * Creates a new user.
     * <p>
     * The method accepts a {@link UserDTO} object, converts it to a {@link User} entity,
     * saves it through the {@code userService}, and returns the saved user as a {@link UserDTO}.
     * </p>
     *
     * @param userDTO The {@link UserDTO} object containing the user data to be saved.
     * @return A {@link ResponseEntity} containing the saved user as a {@link UserDTO}.
     */
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        User user = converter.convertToUser(userDTO);
        user.setPassword(userDTO.getPassword());
        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(converter.convertToUserDTO(savedUser));
    }

    /**
     * Updates an existing user.
     * <p>
     * The method accepts a {@link UserDTO} object with updated data, converts it to a {@link User} entity,
     * and updates the user with the given {@code userId}. If the user is not found,
     * a {@link ResourceNotFoundException} is thrown.
     * </p>
     *
     * @param userId   The ID of the user to be updated.
     * @param userDTO  The {@link UserDTO} object containing the updated user data.
     * @return A {@link ResponseEntity} containing the updated user as a {@link UserDTO}.
     * @throws ResourceNotFoundException If the user with the given {@code userId} is not found.
     */
    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(@Valid @PathVariable Long userId, @RequestBody UserDTO userDTO) {
        User updatedUser = converter.convertToUser(userDTO);
        updatedUser.setUserId(userId);
        User user = userService.updateUser(userId, updatedUser);
        if (user == null) {
            throw new ResourceNotFoundException("User with ID " + userId + " not found");
        }
        return ResponseEntity.ok(converter.convertToUserDTO(user));
    }

    /**
     * Deletes a user by their ID.
     * <p>
     * The method deletes the user associated with the provided {@code userId}.
     * If the user is not found, a {@link ResourceNotFoundException} is thrown.
     * </p>
     *
     * @param userId The ID of the user to be deleted.
     * @return A {@link ResponseEntity} indicating the result of the delete operation.
     * @throws ResourceNotFoundException If the user with the given {@code userId} is not found.
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        boolean isDeleted = userService.deleteUser(userId);
        if (!isDeleted) {
            throw new ResourceNotFoundException("User with ID " + userId + " not found");
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves a user by their ID.
     * <p>
     * The method fetches the user with the given {@code userId}. If the user is found,
     * it returns the user as a {@link UserDTO}. If not, it returns a {@code 404 Not Found} response.
     * </p>
     *
     * @param userId The ID of the user to be retrieved.
     * @return A {@link ResponseEntity} containing the user as a {@link UserDTO} or a {@code 404 Not Found} response.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return user != null ? ResponseEntity.ok(converter.convertToUserDTO(user)) : ResponseEntity.notFound().build();
    }

    /**
     * Retrieves all users with pagination.
     * <p>
     * The method returns a paginated list of all users, with each user represented as a {@link UserDTO}.
     * </p>
     *
     * @param pageable Pageable object for pagination.
     * @return A {@link ResponseEntity} containing the paginated users as a {@link PagedResponseDTO} of {@link UserDTO}.
     */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<UserDTO>> getAllUsers(Pageable pageable) {
        Page<User> users = userService.getAllUsers(pageable);
        List<UserDTO> userDTOs = users.getContent().stream()
                .map(converter::convertToUserDTO)
                .toList();

        PagedResponseDTO<UserDTO> response = createResponse(userDTOs, users);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a paginated response for users.
     * <p>
     * The method converts the users into a {@link PagedResponseDTO} containing user DTOs,
     * along with pagination details.
     * </p>
     *
     * @param userDTOs A list of {@link UserDTO} objects to include in the response.
     * @param users The paginated list of users.
     * @return A {@link PagedResponseDTO} containing the user DTOs and pagination details.
     */
    private PagedResponseDTO<UserDTO> createResponse(List<UserDTO> userDTOs, Page<User> users) {
        return new PagedResponseDTO<>(
                userDTOs,
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages()
        );
    }
}