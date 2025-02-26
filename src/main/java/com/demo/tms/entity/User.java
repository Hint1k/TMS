package com.demo.tms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

/**
 * {@code User} represents a user in the system. A user has attributes such as a username, email, password, role,
 * tasks they created, tasks they are assigned to, and whether the user is enabled. The user also has a unique role
 * associated with them.
 * <p>
 * This class is mapped to the {@code users} table in the database with a unique constraint on the email field
 * to ensure each user has a unique email address.
 * </p>
 * <p>
 * The {@code User} class defines a one-to-one relationship with the {@code Role} entity and a one-to-many
 * relationship with the {@code Task} entity. A user can have multiple tasks assigned to them and
 * can also create multiple tasks.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User implements Serializable {

    /**
     * The unique identifier for the user.
     * This field is automatically generated using an identity strategy for primary key generation.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long userId;

    /**
     * The username of the user.
     * This field is mandatory and represents the user's unique identifier within the system.
     */
    @Column(name = "username", nullable = false, length = 45)
    private String username;

    /**
     * The email address of the user.
     * This field is mandatory and must be unique within the system.
     * It represents the user's primary contact information.
     */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * The password of the user.
     * This field is mandatory and stores the user's password for authentication.
     */
    @Column(name = "password", nullable = false, length = 68)
    private String password;

    /**
     * The role assigned to the user.
     * This field is a one-to-one relationship with the {@code Role} entity.
     * The role represents the user's permissions and access within the system.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Role role; // bidirectional, referencing side

    /**
     * The tasks created by the user.
     * This field is a one-to-many relationship with the {@code Task} entity.
     * A user can create multiple tasks in the system.
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Task> createdTasks;

    /**
     * The tasks assigned to the user.
     * This field is a one-to-many relationship with the {@code Task} entity.
     * A user can be assigned to multiple tasks in the system.
     */
    @OneToMany(mappedBy = "assignee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Task> assignedTasks;

    /**
     * A flag indicating whether the user is enabled.
     * This field is mandatory and determines whether the user is allowed to log in and use the system.
     */
    @Column(name = "enabled", nullable = false)
    private boolean isEnabled;

    /**
     * Constructor used for creating a new user without an ID.
     * This constructor is typically used when creating a new user in the system.
     *
     * @param username The username of the user.
     * @param email The email address of the user.
     * @param password The password of the user.
     * @param role The role assigned to the user.
     * @param createdTasks The tasks created by the user.
     * @param assignedTasks The tasks assigned to the user.
     * @param isEnabled A flag indicating whether the user is enabled.
     */
    public User(String username, String email, String password, Role role, Set<Task> createdTasks,
                Set<Task> assignedTasks, boolean isEnabled) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdTasks = createdTasks;
        this.assignedTasks = assignedTasks;
        this.isEnabled = isEnabled;
    }
}