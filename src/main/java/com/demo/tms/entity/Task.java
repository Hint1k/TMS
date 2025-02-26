package com.demo.tms.entity;

import com.demo.tms.utils.TaskPriority;
import com.demo.tms.utils.TaskStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code Task} represents a task within the system. A task includes details such as its name, description, status,
 * priority, the author who created it, the assignee who is responsible for it, a list of associated comments,
 * and a version for concurrency control.
 * <p>
 * This class is mapped to the {@code tasks} table in the database and uses JPA annotations for
 * object-relational mapping (ORM).
 * </p>
 * <p>
 * The {@code Task} class includes fields for the task's ID, name, description, status, priority, author, assignee,
 * comments, and version. It also defines a one-to-many relationship with comments, where each task can have multiple
 * comments associated with it.
 * </p>
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tasks")
public class Task implements Serializable {

    /**
     * The unique identifier for the task.
     * This field is automatically generated using an identity strategy for primary key generation.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long taskId;

    /**
     * The name of the task.
     * This field is mandatory and represents the title or summary of the task.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * The description of the task.
     * This field is mandatory and provides detailed information about the task.
     */
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    /**
     * The current status of the task.
     * This field is mandatory and represents the state of the task, such as "IN_PROGRESS" or "COMPLETED".
     * The status is represented as an enumerated value.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 45)
    private TaskStatus status;

    /**
     * The priority level of the task.
     * This field is mandatory and represents the importance of the task, such as "HIGH" or "LOW".
     * The priority is represented as an enumerated value.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 45)
    private TaskPriority priority;

    /**
     * The author of the task.
     * This field is a many-to-one relationship with the {@code User} entity.
     * The author is the user who created the task.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * The assignee of the task.
     * This field is a many-to-one relationship with the {@code User} entity.
     * The assignee is the user responsible for completing the task.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", nullable = false)
    private User assignee;

    /**
     * A list of comments associated with the task.
     * This field defines a one-to-many relationship with the {@code Comment} entity.
     * A task can have multiple comments associated with it.
     */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    /**
     * The version of the task, used for concurrency control.
     * This field ensures that updates to the task are done in a consistent manner,
     * preventing race conditions when multiple users are trying to update the task simultaneously.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /**
     * Constructor used for creating a new task without an ID.
     * This constructor is typically used when creating a new task in the system.
     *
     * @param name The name of the task.
     * @param description The description of the task.
     * @param status The status of the task.
     * @param priority The priority of the task.
     * @param author The user who created the task.
     * @param assignee The user assigned to the task.
     * @param comments A list of comments associated with the task.
     * @param version The version of the task for concurrency control.
     */
    public Task(String name, String description, TaskStatus status, TaskPriority priority, User author,
                User assignee, List<Comment> comments, Long version) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.author = author;
        this.assignee = assignee;
        this.comments = comments;
        this.version = version;
    }
}