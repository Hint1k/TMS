package com.demo.tms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * {@code Comment} represents a comment made by a user on a specific task in the system.
 * Each comment includes the comment's text, the user who made the comment, the task to which the comment is attached,
 * and a version number for optimistic locking.
 * <p>
 * This class is mapped to the {@code comments} table in the database, and it utilizes JPA annotations for ORM mapping.
 * </p>
 * <p>
 * The {@code Comment} class includes fields for the comment's ID, text, associated user, associated task,
 * and version control for concurrency management.
 * </p>
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "comments")
public class Comment implements Serializable {

    /**
     * The unique identifier for the comment.
     * This field is automatically generated using an identity strategy for primary key generation.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long commentId;

    /**
     * The text content of the comment.
     * This field is mandatory and must be a non-null text value.
     */
    @Column(name = "text", columnDefinition = "TEXT", nullable = false)
    private String text;

    /**
     * The user who made the comment.
     * This is a many-to-one relationship with the {@code User} entity.
     * The field is mandatory and cannot be null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The task to which the comment is attached.
     * This is a many-to-one relationship with the {@code Task} entity.
     * The field is mandatory and cannot be null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /**
     * The version number for optimistic locking.
     * This field ensures that the comment is handled correctly in concurrent environments.
     * The version is automatically managed by JPA.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /**
     * Constructor used for creating a new comment without an ID.
     * This constructor is typically used when creating a new comment.
     *
     * @param text    The text content of the comment.
     * @param user    The user who made the comment.
     * @param task    The task to which the comment is attached.
     * @param version The version number for optimistic locking.
     */
    public Comment(String text, User user, Task task, Long version) {
        this.text = text;
        this.user = user;
        this.task = task;
        this.version = version;
    }
}