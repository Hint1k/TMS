package com.demo.tms.repository;

import com.demo.tms.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * {@code RoleRepository} is a Spring Data JPA repository interface for performing CRUD operations
 * related to {@link Role} entities in the database.
 * <p>
 * This repository allows for basic operations on the {@link Role} entity, such as saving, deleting,
 * and querying roles by their primary key.
 * </p>
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
}