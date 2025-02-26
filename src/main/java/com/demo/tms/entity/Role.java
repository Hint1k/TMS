package com.demo.tms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * {@code Role} represents a role or authority assigned to a user within the system.
 * Each role includes the role's unique identifier, the authority (or role name),
 * and a reference to the user who is assigned that role.
 * <p>
 * This class is mapped to the {@code authorities} table in the database and uses JPA annotations
 * for object-relational mapping (ORM).
 * </p>
 * <p>
 * The {@code Role} class includes fields for the role's ID, authority name, and a one-to-one relationship
 * with the user. The user is the owning side of the relationship, and each role is uniquely assigned to one user.
 * </p>
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "authorities")
public class Role implements Serializable {

    /**
     * The unique identifier for the role.
     * This field is automatically generated using an identity strategy for primary key generation.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long roleId;

    /**
     * The authority (or role name) associated with the role.
     * This field is mandatory and represents the role's function or permission in the system.
     */
    @Column(name = "authority", nullable = false)
    private String authority;

    /**
     * The user associated with the role.
     * This is a one-to-one relationship with the {@code User} entity.
     * The field is mandatory and cannot be null, meaning each role is uniquely assigned to one user.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user; // bidirectional, owning side

    /**
     * Constructor used for creating a new role without an ID.
     * This constructor is typically used when creating a new role for a user.
     *
     * @param authority The authority (role name) of the role.
     * @param user The user assigned this role.
     */
    public Role(String authority, User user) {
        this.authority = authority;
        this.user = user;
    }
}