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

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final Converter converter;

    @Autowired
    public UserController(UserService userService, Converter converter) {
        this.userService = userService;
        this.converter = converter;
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        User user = converter.convertToUser(userDTO);
        user.setPassword(userDTO.getPassword());
        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(converter.convertToUserDTO(savedUser));
    }

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

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        boolean isDeleted = userService.deleteUser(userId);
        if (!isDeleted) {
            throw new ResourceNotFoundException("User with ID " + userId + " not found");
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return user != null ? ResponseEntity.ok(converter.convertToUserDTO(user)) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<PagedResponseDTO<UserDTO>> getAllUsers(Pageable pageable) {
        Page<User> users = userService.getAllUsers(pageable);
        List<UserDTO> userDTOs = users.getContent().stream()
                .map(converter::convertToUserDTO)
                .toList();

        PagedResponseDTO<UserDTO> response = createResponse(userDTOs, users);
        return ResponseEntity.ok(response);
    }

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