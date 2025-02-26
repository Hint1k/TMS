package com.demo.tms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long userId;

    @Column(name = "username", nullable = false, length = 45)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 68)
    private String password;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Role role; // bidirectional, referencing side

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Task> createdTasks;

    @OneToMany(mappedBy = "assignee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Task> assignedTasks;

    @Column(name = "enabled", nullable = false)
    private boolean isEnabled;

    // no userId
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