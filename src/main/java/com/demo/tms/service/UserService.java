package com.demo.tms.service;

import com.demo.tms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    User saveUser(User user);

    User updateUser(Long userId, User updatedUser);

    boolean deleteUser(Long userId);

    User getUserById(Long userId);

    Page<User> getAllUsers(Pageable pageable);
}
