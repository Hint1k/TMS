package com.demo.tms.repository;

import com.demo.tms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * {@code UserRepository} is a Spring Data JPA repository interface for performing CRUD operations
 * related to {@link User} entities in the database.
 * <p>
 * This repository provides a custom query method to find a user by their email address.
 * </p>
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     *
     * @param email the email address of the user
     * @return an {@link Optional} containing the {@link User} if found, or an empty {@link Optional} if not
     */
    @Query("SELECT u FROM User u WHERE u.email = ?1")
    Optional<User> findByEmail(String email);
}