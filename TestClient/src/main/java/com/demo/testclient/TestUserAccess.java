package com.demo.testclient;

import com.demo.testclient.dto.TaskDTO;
import com.demo.testclient.enums.TaskPriority;
import com.demo.testclient.enums.TaskStatus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootApplication
public class TestUserAccess {

    private static String adminToken;
    private static String user1Token;
    private static String user2Token;

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext context = SpringApplication.run(TestUserAccess.class, args);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());


        String baseUrl = "http://localhost:8080/api";
        String loginUrl = "http://localhost:8080/auth/login";

        // Authenticate different users
        adminToken = getJwtToken(restTemplate, loginUrl, "admin@example.com", "123");
        user1Token = getJwtToken(restTemplate, loginUrl, "user1@example.com", "123");
        user2Token = getJwtToken(restTemplate, loginUrl, "user2@example.com", "123");

        // Admin creates two tasks with specific authors and assignees
        Long task1Id = createTask(restTemplate, baseUrl, adminToken, "Task by User1", "Description 1",
                TaskStatus.PENDING, TaskPriority.HIGH, 2L, 3L);
        Long task2Id = createTask(restTemplate, baseUrl, adminToken, "Task by Admin", "Description 2",
                TaskStatus.PENDING, TaskPriority.MEDIUM, 1L, 1L);

        // Test access rights
        testUserAccess(restTemplate, baseUrl, user1Token, task1Id, task2Id, "User1");
        testUserAccess(restTemplate, baseUrl, user2Token, task1Id, task2Id, "User2");

        // New Test Cases
        testAssigneeStatusUpdate(restTemplate, baseUrl, user2Token, task1Id, task2Id);
        testAssigneeCreateComment(restTemplate, baseUrl, user2Token, task1Id, task2Id, 3L);
        testAssigneeTaskUpdate(restTemplate, baseUrl, user2Token, task1Id, task2Id);
        testAssigneeCreateTask(restTemplate, baseUrl, user2Token);
        testAuthorCreateTask(restTemplate, baseUrl, user1Token);
        testAuthorReadAllTasks(restTemplate, baseUrl, user1Token);
        testAuthorReadAllComments(restTemplate, baseUrl, user1Token);

        testAdminCreateUser(restTemplate, baseUrl, adminToken);
        testAdminCreateRole(restTemplate, baseUrl, adminToken);
        Long id2 = testUserCreateUser(restTemplate, baseUrl, user1Token);
        testUserCreateRole(restTemplate, baseUrl, user2Token);
        testUserUpdateUser(restTemplate, baseUrl, user2Token, id2);
        Thread.sleep(1000);
        System.exit(1);
    }

    private static String getJwtToken(RestTemplate restTemplate, String url, String email, String password) {
        URI loginUrl = UriComponentsBuilder.fromHttpUrl(url).queryParam("email", email)
                .queryParam("password", password).build().toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map<String, String>> response = restTemplate.exchange(loginUrl, HttpMethod.POST, entity,
                new ParameterizedTypeReference<>() {
                });
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody().get("token");
        } else {
            throw new RuntimeException("Failed to login, status: " + response.getStatusCode());
        }
    }

    private static <T> HttpEntity<T> createEntity(T body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        return new HttpEntity<>(body, headers);
    }

    private static Long createTask(RestTemplate restTemplate, String baseUrl, String token, String name,
                                   String description, TaskStatus status, TaskPriority priority,
                                   Long authorId, Long assigneeId) {
        URI createTaskUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tasks").build().toUri();
        TaskDTO taskToCreate = new TaskDTO(name, description, status, priority, authorId, assigneeId, null);
        HttpEntity<TaskDTO> entity = createEntity(taskToCreate, token);

        try {
            TaskDTO createdTask = restTemplate.exchange(createTaskUrl, HttpMethod.POST, entity, TaskDTO.class).getBody();
            System.out.println("Created Task (ID " + createdTask.getTaskId() + "): " + createdTask);
            return createdTask.getTaskId();
        } catch (HttpClientErrorException.Forbidden e) {
            System.out.println("SUCCESS: Assignee was correctly denied task creation (403 Forbidden).");
            return null;  // Indicate that the request was correctly denied
        } catch (HttpClientErrorException e) {
            System.out.println("ERROR: Unexpected failure: " + e.getStatusCode());
            return null;
        }
    }

    private static void testUserAccess(RestTemplate restTemplate, String baseUrl, String token,
                                       Long accessibleTaskId, Long restrictedTaskId, String user) {
        System.out.println("Testing access for " + user + "...");

        // Check access to assigned task (should succeed)
        boolean success1 = testTaskAccess(restTemplate, baseUrl, token, accessibleTaskId, true, user);
        if (success1) {
            System.out.println(user + " correctly accessed Task ID " + accessibleTaskId);
        } else {
            System.out.println("ERROR: " + user + " should have access to Task ID " + accessibleTaskId + " but was denied!");
        }

        // Check access to other task (should fail)
        boolean success2 = testTaskAccess(restTemplate, baseUrl, token, restrictedTaskId, false, user);
        if (!success2) {
            System.out.println(user + " correctly denied access to Task ID " + restrictedTaskId);
        } else {
            System.out.println("ERROR: " + user + " should NOT have access to Task ID " + restrictedTaskId + " but was granted access!");
        }
    }

    private static boolean testTaskAccess(RestTemplate restTemplate, String baseUrl, String token,
                                          Long taskId, boolean shouldSucceed, String user) {
        URI getTaskUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tasks/" + taskId).build().toUri();
        HttpEntity<String> entity = createEntity(null, token);
        try {
            ResponseEntity<TaskDTO> response = restTemplate.exchange(getTaskUrl, HttpMethod.GET, entity, TaskDTO.class);
            System.out.println(user + " attempted access to Task ID " + taskId + " SUCCESS: " + response.getBody());
            return shouldSucceed;
        } catch (HttpClientErrorException e) {
            System.out.println(user + " attempted access to Task ID " + taskId + " DENIED: " + e.getStatusCode());
            return !shouldSucceed;
        }
    }

    private static void testAssigneeStatusUpdate(RestTemplate restTemplate, String baseUrl, String token,
                                                 Long allowedTaskId, Long restrictedTaskId) {
        System.out.println("Testing status update for assignee...");

        // Assignee should be able to update the status of allowed task
        boolean success1 = updateTaskStatus(restTemplate, baseUrl, token, allowedTaskId, TaskStatus.PROCESSING, true);

        // Assignee should NOT be able to update status of restricted task
        boolean success2 = updateTaskStatus(restTemplate, baseUrl, token, restrictedTaskId, TaskStatus.PROCESSING, false);

        if (success1) {
            System.out.println("SUCCESS: Assignee updated status of Task ID " + allowedTaskId);
        }
        if (!success2) {
            System.out.println("SUCCESS: Assignee was correctly denied updating Task ID " + restrictedTaskId);
        }
    }

    private static void testAssigneeCreateComment(RestTemplate restTemplate, String baseUrl, String token,
                                                  Long allowedTaskId, Long restrictedTaskId, Long userId) {
        System.out.println("Testing comment creation for assignee...");

        // Assignee should be able to create comment for allowed task
        boolean success1 = createComment(restTemplate, baseUrl, token, allowedTaskId, true, userId);

        if (success1) {
            System.out.println("SUCCESS: Assignee created comment for Task ID " + allowedTaskId);
        } else {
            System.out.println("ERROR: Assignee was denied creating comment for Task ID " + allowedTaskId);
        }

        // Assignee should NOT be able to create comment for restricted task
        boolean success2 = createComment(restTemplate, baseUrl, token, restrictedTaskId, false, userId);
        if (!success2) {
            System.out.println("SUCCESS: Assignee was correctly denied commenting on Task ID " + restrictedTaskId);
        } else {
            System.out.println("ERROR: Assignee was incorrectly allowed to create comment for Task ID " + restrictedTaskId);
        }
    }

    private static void testAssigneeTaskUpdate(RestTemplate restTemplate, String baseUrl, String token,
                                               Long task1Id, Long task2Id) {
        System.out.println("Testing task update for assignee...");

        boolean success1 = updateTask(restTemplate, baseUrl, token, task1Id, false);
        boolean success2 = updateTask(restTemplate, baseUrl, token, task2Id, false);

        if (!success1) {
            System.out.println("SUCCESS: Assignee was correctly denied updating Task ID " + task1Id);
        }
        if (!success2) {
            System.out.println("SUCCESS: Assignee was correctly denied updating Task ID " + task2Id);
        }
    }

    private static void testAssigneeCreateTask(RestTemplate restTemplate, String baseUrl, String token) {
        System.out.println("Testing task creation for assignee...");

        Long taskId = createTask(restTemplate, baseUrl, token, "Valid Task", "Should succeed",
                TaskStatus.PENDING, TaskPriority.LOW, 3L, 2L);

        if (taskId != null) {
            System.out.println("SUCCESS: Assignee was able to create a task.");
        } else {
            System.out.println("ERROR: Assignee should be able to create a task, but was denied access!");
        }
    }

    private static void testAuthorCreateTask(RestTemplate restTemplate, String baseUrl, String token) {
        System.out.println("Testing task creation for author...");

        boolean success = createTask(restTemplate, baseUrl, token, "Author's Task", "Should succeed",
                TaskStatus.PENDING, TaskPriority.MEDIUM, 2L, 3L) != null;

        if (success) {
            System.out.println("SUCCESS: Author was able to create a task.");
        }
    }

    private static void testAuthorReadAllTasks(RestTemplate restTemplate, String baseUrl, String token) {
        System.out.println("Testing author reading all tasks...");

        boolean success = getAllTasks(restTemplate, baseUrl, token);
        if (success) {
            System.out.println("SUCCESS: Author was able to read all tasks.");
        }
    }

    private static void testAuthorReadAllComments(RestTemplate restTemplate, String baseUrl, String token) {
        System.out.println("Testing author reading all comments...");

        boolean success = getAllComments(restTemplate, baseUrl, token);
        if (success) {
            System.out.println("SUCCESS: Author was able to read all comments.");
        }
    }

    private static boolean updateTaskStatus(RestTemplate restTemplate, String baseUrl, String token,
                                            Long taskId, TaskStatus newStatus, boolean shouldSucceed) {
        URI updateUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tasks/" + taskId + "/status").build().toUri();
        HttpEntity<Map<String, String>> entity = createEntity(Map.of("status", newStatus.name()), token);

        try {
            restTemplate.exchange(updateUrl, HttpMethod.PATCH, entity, Void.class);
            return shouldSucceed;
        } catch (HttpClientErrorException e) {
            return !shouldSucceed;
        }
    }

    private static boolean createComment(RestTemplate restTemplate, String baseUrl, String token,
                                         Long taskId, boolean shouldSucceed, Long userId) {
        URI commentUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/comments").build().toUri();
        HttpEntity<Map<String, Object>> entity = createEntity(Map.of("text", "Test Comment", "taskId",
                taskId, "userId", userId), token);

        try {
            restTemplate.exchange(commentUrl, HttpMethod.POST, entity, Void.class);
            return shouldSucceed;
        } catch (HttpClientErrorException e) {
            return !shouldSucceed;
        }
    }


    private static boolean updateTask(RestTemplate restTemplate, String baseUrl, String token,
                                      Long taskId, boolean shouldSucceed) {
        URI updateUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tasks/" + taskId).build().toUri();
        HttpEntity<TaskDTO> entity = createEntity(new TaskDTO(taskId, "Updated Name", "Updated Description",
                TaskStatus.PROCESSING, TaskPriority.HIGH, 1L, 1L, null), token);

        try {
            restTemplate.exchange(updateUrl, HttpMethod.PUT, entity, Void.class);
            return shouldSucceed;
        } catch (HttpClientErrorException e) {
            return !shouldSucceed;
        }
    }

    private static boolean getAllTasks(RestTemplate restTemplate, String baseUrl, String token) {
        URI getAllTasksUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tasks").build().toUri();
        HttpEntity<String> entity = createEntity(null, token);

        try {
            restTemplate.exchange(getAllTasksUrl, HttpMethod.GET, entity, Void.class);
            System.out.println("SUCCESS: User was granted access to GET /tasks");
            return true;
        } catch (HttpClientErrorException e) {
            System.out.println("SUCCESS: User was correctly denied access to GET /tasks - " + e.getStatusCode());
            return false;
        }
    }

    private static boolean getAllComments(RestTemplate restTemplate, String baseUrl, String token) {
        URI getAllCommentsUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/comments").build().toUri();
        HttpEntity<String> entity = createEntity(null, token);

        try {
            restTemplate.exchange(getAllCommentsUrl, HttpMethod.GET, entity, Void.class);
            System.out.println("SUCCESS: User was granted access to GET /comments");
            return true;
        } catch (HttpClientErrorException e) {
            System.out.println("SUCCESS: User was correctly denied access to GET /comments - " + e.getStatusCode());
            return false;
        }
    }

    private static void testAdminCreateRole(RestTemplate restTemplate, String baseUrl, String token) {
        System.out.println("Testing role creation by admin...");

        Long roleId = createRole(restTemplate, baseUrl, token, "Admin Role");

        if (roleId != null) {
            System.out.println("SUCCESS: Admin was able to create a new role.");
        } else {
            System.out.println("ERROR: Admin should be able to create a new role, but was denied!");
        }
    }

    private static void testUserCreateRole(RestTemplate restTemplate, String baseUrl, String token) {
        System.out.println("Testing role creation by a non-admin (User ID 3)...");

        Long roleId = createRole(restTemplate, baseUrl, token, "Unauthorized Role");

        if (roleId == null) {
            System.out.println("SUCCESS: Non-admin user was correctly denied role creation.");
        } else {
            System.out.println("ERROR: Non-admin user should NOT be able to create a new role, but was allowed!");
        }
    }

    private static Long createRole(RestTemplate restTemplate, String baseUrl, String token, String authority) {
        URI createRoleUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/roles").build().toUri();
        HttpEntity<Map<String, String>> entity = createEntity(Map.of(
                "authority", authority,
                "userId", "4"
        ), token);
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(createRoleUrl, HttpMethod.POST, entity,
                    new ParameterizedTypeReference<>() {
                    });
            return response.getBody() != null ? ((Number) response.getBody().get("roleId")).longValue() : null;
        } catch (HttpClientErrorException.Forbidden e) {
            return null;
        }
    }

    private static Long testAdminCreateUser(RestTemplate restTemplate, String baseUrl, String token) {
        System.out.println("Testing user creation by admin...");
        // Generate a random email address using UUID
        String randomEmail = UUID.randomUUID().toString() + "@example.com";
        Long userId = createUser(restTemplate, baseUrl, token, randomEmail, "NewUser", "123456");

        if (userId != null) {
            System.out.println("SUCCESS: Admin was able to create a new user with email " + randomEmail);
            return userId;
        } else {
            System.out.println("ERROR: Admin should be able to create a new user, but was denied!");
            return null;
        }
    }

    private static void testUserUpdateUser(RestTemplate restTemplate, String baseUrl, String token, Long userId) {
        System.out.println("Testing user update by another user (User ID 2)...");

        boolean success = updateUser(restTemplate, baseUrl, token, userId, "UnauthorizedUpdate");

        if (!success) {
            System.out.println("SUCCESS: User was correctly denied updating another user.");
        } else {
            System.out.println("ERROR: User should NOT be able to update another user, but was allowed!");
        }
    }

    private static Long testUserCreateUser(RestTemplate restTemplate, String baseUrl, String token) {
        System.out.println("Testing user creation by a non-admin (User ID 3)...");

        Long userId = createUser(restTemplate, baseUrl, token, "unauthorized@example.com",
                "UnauthorizedUser", "123456");

        if (userId == null) {
            System.out.println("SUCCESS: Non-admin user was correctly denied user creation.");
            return userId;
        } else {
            System.out.println("ERROR: Non-admin user should NOT be able to create a new user, but was allowed!");
            return null;
        }
    }

    private static Long createUser(RestTemplate restTemplate, String baseUrl, String token, String email, String username,
                                   String password) {
        URI createUserUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/users").build().toUri();
        HttpEntity<Map<String, String>> entity = createEntity(Map.of(
                "username", username,
                "email", email,
                "password", password
        ), token);
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(createUserUrl, HttpMethod.POST, entity,
                    new ParameterizedTypeReference<>() {
                    });
            return response.getBody() != null ? ((Number) response.getBody().get("userId")).longValue() : null;
        } catch (HttpClientErrorException.Forbidden e) {
            return null;
        }
    }

    private static boolean updateUser(RestTemplate restTemplate, String baseUrl, String token, Long userId, String newName) {
        URI updateUserUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/users/" + userId).build().toUri();
        HttpEntity<Map<String, String>> entity = createEntity(Map.of("username", newName), token);

        try {
            restTemplate.exchange(updateUserUrl, HttpMethod.PUT, entity, Void.class);
            return true;
        } catch (HttpClientErrorException.Forbidden e) {
            return false;
        }
    }
}