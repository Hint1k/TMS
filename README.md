# Task Management System

## Introduction

The Task Management System (TMS) is designed to manage tasks, users, roles, and permissions in a collaborative environment. 
The system allows for task creation, assignment, status tracking, and user management.
The system integrates role-based access control (RBAC) to enforce security and ensure users have appropriate access levels.

This README provides basic installation and usage instructions for the TMS


## Installation and Running

### Downloading TMS

The Task Management System source code can be downloaded from the repository:

```bash
git clone https://github.com/Hint1k/TMS.git

```

### Using TMS

To use the Task Management System run it in Docker:

```bash
docker-compose up --build
```

### Swagger API documentation

Swagger API documentation can be accessed after starting the application via the browser:

```bash
http://localhost:8080/swagger-ui/index.html
```

### Generating Application Documentation

To generate the application's Javadoc, run the following command:

```bash
./gradlew javadoc
```

## Additional Features

- **Optimistic Locking**: The application uses optimistic locking to ensure safe concurrent updates.
- **Caching**: It uses Spring built-in caching for improved performance and quick access to frequently used data.

## User rights:

- Admins have full control over all tasks (create, read, update, delete)
- Task creators have full control over the tasks they created (create, read, update, delete their own tasks)
- Assignees have limited control over tasks assigned to them (change status and leave comments)

## Database

1. The database SQL script is located in **src/main/resources/sql-scripts/init.sql**
2. It pre-fills the database with sample users, roles, tasks, and comments
3. It sets a default admin account with the username **admin@example.com** and password **123**

## Test Client

The TestClient folder contains files that simulate a client sending requests to the application APIs. 
In order to use it, run the application first and then run the test client classes in your IDE:

- TestTms class: imitates CRUD operations.
- TestUserAccess class: checks user rights.

## Technological Stack

The Task Management System is built using the following technologies:

- Java
- Spring Boot (including Data JPA, Security, Web, Caching, Retry)
- Hibernate (JPA implementation)
- Gradle
- PostgreSQL 
- Swagger (OpenAPI)
- Docker