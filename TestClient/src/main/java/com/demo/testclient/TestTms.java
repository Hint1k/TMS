package com.demo.testclient;

import com.demo.testclient.dto.CommentDTO;
import com.demo.testclient.dto.PagedResponseDTO;
import com.demo.testclient.dto.TaskDTO;
import com.demo.testclient.enums.TaskPriority;
import com.demo.testclient.enums.TaskStatus;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class TestTms {

    private static String token;

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext context = SpringApplication.run(TestTms.class, args);
        RestTemplate restTemplate = new RestTemplate();
        String baseUrl = "http://localhost:8080/api";
        String email = "admin@example.com";
        String loginUrl = "http://localhost:8080/auth/login";
        token = testAuthenticationEndpoint(restTemplate, loginUrl, email, "123");

        // test methods:
        testTaskControllerEndpoints(restTemplate, baseUrl);
        testCommentControllerEndpoints(restTemplate, baseUrl);
        testCaching(restTemplate, baseUrl);
        testOptimisticLocking(restTemplate, baseUrl);
        testExceptionHandling(restTemplate, baseUrl);
        testConcurrentTaskCreation(restTemplate, baseUrl);
        testTransactionRollback(restTemplate, baseUrl);
        Thread.sleep(1000);
        System.exit(1);
    }

    // Helper method to create HttpEntity with the token and request body
    private static <T> HttpEntity<T> createEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        return new HttpEntity<>(body, headers);
    }

    private static String getJwtToken(RestTemplate restTemplate, String url, String email, String password) {
        URI loginUrl = UriComponentsBuilder.fromHttpUrl(url).queryParam("email", email)
                .queryParam("password", password).build().toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON)); // Expect JSON response
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map<String, String>> response = restTemplate.exchange(loginUrl, HttpMethod.POST, entity,
                new ParameterizedTypeReference<>() {});
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody().get("token"); // Extract JWT token
        } else {
            throw new RuntimeException("Failed to login, status: " + response.getStatusCode());
        }
    }

    private static String testAuthenticationEndpoint(RestTemplate restTemplate, String url, String email,
                                                     String password) {
        return getJwtToken(restTemplate, url, email, password);
    }

    private static void testTaskControllerEndpoints(RestTemplate restTemplate, String baseUrl) {
        System.out.println("Testing TaskController Endpoints...");

        // 1. Create Task
        URI createTaskUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tasks").build().toUri();
        TaskDTO taskToCreate = new TaskDTO("Test Task", "This is a test task.",
                TaskStatus.PENDING, TaskPriority.MEDIUM, 1L, 2L, null);

        HttpEntity<TaskDTO> entity = createEntity(taskToCreate);
        TaskDTO createdTask = restTemplate.exchange(createTaskUrl, HttpMethod.POST, entity, TaskDTO.class).getBody();
        System.out.println("Created Task: " + createdTask);

        // 2. Get Task by ID
        Long taskId = createdTask.getTaskId();
        URI getTaskByIdUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tasks/" + taskId).build().toUri();
        TaskDTO retrievedTask = restTemplate.exchange(getTaskByIdUrl, HttpMethod.GET, entity, TaskDTO.class).getBody();
        System.out.println("Retrieved Task by ID: " + retrievedTask);

        // 3. Update Task
        TaskDTO updatedTask = new TaskDTO(taskId, "Updated Task", "This task has been updated.",
                TaskStatus.PROCESSING, TaskPriority.HIGH, 1L, 2L, null);
        URI updateTaskUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tasks/" + taskId).build().toUri();
        HttpEntity<TaskDTO> requestEntity = createEntity(updatedTask);
        ResponseEntity<TaskDTO> response =
                restTemplate.exchange(updateTaskUrl, HttpMethod.PUT, requestEntity, TaskDTO.class);
        TaskDTO resultAfterUpdate = response.getBody();
        System.out.println("Updated Task: " + resultAfterUpdate);

        // 4. Delete Task
        URI deleteTaskUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tasks/" + taskId).build().toUri();
        restTemplate.exchange(deleteTaskUrl, HttpMethod.DELETE, entity, Void.class);
        System.out.println("Deleted Task with ID: " + taskId);

        // 5. Get All Tasks
        URI getAllTasksUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tasks").queryParam("page", 0)
                .queryParam("size", 10).build().toUri();
        ResponseEntity<PagedResponseDTO<TaskDTO>> allTasks = restTemplate.exchange(getAllTasksUrl, HttpMethod.GET,
                entity, new ParameterizedTypeReference<>() {});
        PagedResponseDTO<TaskDTO> allTasksPage = allTasks.getBody();
        System.out.println("All Tasks: " + allTasksPage);
    }

    private static void testCommentControllerEndpoints(RestTemplate restTemplate, String baseUrl) {
        System.out.println("Testing CommentController Endpoints...");

        // 1. Create Comment
        URI createCommentUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/comments").build().toUri();
        CommentDTO commentToCreate = new CommentDTO("This is a test comment.", 1L, 1L);
        HttpEntity<CommentDTO> entity = createEntity(commentToCreate);
        CommentDTO createdComment =
                restTemplate.exchange(createCommentUrl, HttpMethod.POST, entity, CommentDTO.class).getBody();
        System.out.println("Created Comment: " + createdComment);

        // 2. Get Comment by ID
        Long commentId = createdComment.getCommentId();
        URI getCommentByIdUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/comments/" + commentId).build().toUri();
        CommentDTO retrievedComment =
                restTemplate.exchange(getCommentByIdUrl, HttpMethod.GET, entity, CommentDTO.class).getBody();
        System.out.println("Retrieved Comment by ID: " + retrievedComment);

        // 3. Update Comment
        CommentDTO updatedComment = new CommentDTO(commentId, "Updated comment text.", 1L, 1L);
        URI updateCommentUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/comments/" + commentId).build().toUri();
        HttpEntity<CommentDTO> requestEntity = createEntity(updatedComment);
        ResponseEntity<CommentDTO> response =
                restTemplate.exchange(updateCommentUrl, HttpMethod.PUT, requestEntity, CommentDTO.class);
        CommentDTO resultAfterCommentUpdate = response.getBody();
        System.out.println("Updated Comment: " + resultAfterCommentUpdate);

        // 5. Get All Comments
        URI getAllCommentsUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/comments").queryParam("page",
                0).queryParam("size", 10).build().toUri();
        ResponseEntity<PagedResponseDTO<CommentDTO>> allComments = restTemplate.exchange(getAllCommentsUrl,
                HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
        PagedResponseDTO<CommentDTO> allCommentsPage = allComments.getBody();
        System.out.println("All Comments: " + allCommentsPage);
    }

    private static void testCaching(RestTemplate restTemplate, String baseUrl) {
        System.out.println("Testing Caching...");

        // 1. First request to cache a task
        URI getTaskByIdUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tasks/1").build().toUri();

        // Create HttpEntity using the helper method
        HttpEntity<TaskDTO> entity = createEntity(null); // No body for GET request

        TaskDTO firstRequest = restTemplate.exchange(getTaskByIdUrl, HttpMethod.GET, entity, TaskDTO.class).getBody();
        System.out.println("First Request (Cache Miss): " + firstRequest);

        // 2. Second request to retrieve from cache
        TaskDTO secondRequest = restTemplate.exchange(getTaskByIdUrl, HttpMethod.GET, entity, TaskDTO.class).getBody();
        System.out.println("Second Request (Cache Hit): " + secondRequest);
    }

    private static void testOptimisticLocking(RestTemplate restTemplate, String baseUrl) {
        System.out.println("Testing Optimistic Locking...");

        URI updateTaskUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tasks/1").build().toUri();

        // Thread 1: Update task
        new Thread(() -> {
            TaskDTO updatedTask1 = new TaskDTO(1L, "Task Updated by Thread 1",
                    "Thread 1 update.", TaskStatus.COMPLETED, TaskPriority.HIGH,
                    1L, 2L, null);
            HttpEntity<TaskDTO> requestEntity1 = new HttpEntity<>(updatedTask1);
            try {
                restTemplate.exchange(updateTaskUrl, HttpMethod.PUT, requestEntity1, TaskDTO.class);
                System.out.println("Thread 1 Updated Task");
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.CONFLICT) {
                    System.out.println("Thread 1 encountered Optimistic Locking Conflict");
                }
            }
        }).start();

        // Thread 2: Simultaneous Update
        new Thread(() -> {
            TaskDTO updatedTask2 = new TaskDTO(1L, "Task Updated by Thread 2",
                    "Thread 2 update.", TaskStatus.PROCESSING, TaskPriority.MEDIUM,
                    1L, 2L, null);
            HttpEntity<TaskDTO> requestEntity2 = new HttpEntity<>(updatedTask2);
            try {
                restTemplate.exchange(updateTaskUrl, HttpMethod.PUT, requestEntity2, TaskDTO.class);
                System.out.println("Thread 2 Updated Task");
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.CONFLICT) {
                    System.out.println("Thread 2 encountered Optimistic Locking Conflict");
                }
            }
        }).start();
    }

    private static void testExceptionHandling(RestTemplate restTemplate, String baseUrl) {
        System.out.println("Testing Exception Handling...");

        // Trigger Bad Request (400)
        URI badRequestUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tasks/invalid-id").build().toUri();

        // Create HttpEntity using the helper method (no body for GET request)
        HttpEntity<TaskDTO> entity = createEntity(null);

        try {
            restTemplate.exchange(badRequestUrl, HttpMethod.GET, entity, TaskDTO.class);
        } catch (HttpClientErrorException e) {
            System.out.println("Handled Exception: " + e.getStatusCode());
        }
    }

    private static void testConcurrentTaskCreation(RestTemplate restTemplate, String baseUrl) {
        System.out.println("Testing Concurrent Task Creation...");

        // Simultaneous Task Creation from different threads
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                URI createTaskUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tasks")
                        .build().toUri();
                TaskDTO taskToCreate = new TaskDTO("Test Concurrent Task",
                        "This task is created concurrently.", TaskStatus.PENDING, TaskPriority.MEDIUM,
                        1L, 2L, null);

                // Create HttpEntity using the helper method
                HttpEntity<TaskDTO> entity = createEntity(taskToCreate);

                restTemplate.exchange(createTaskUrl, HttpMethod.POST, entity, TaskDTO.class);
            }).start();
        }
    }

    private static void testTransactionRollback(RestTemplate restTemplate, String baseUrl) {
        System.out.println("Testing Transaction Rollback...");

        URI createTaskUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tasks").build().toUri();
        TaskDTO taskToCreate = new TaskDTO("Task with Rollback", "This task will trigger rollback.",
                TaskStatus.PENDING, TaskPriority.LOW, 1L, 2L, null);

        // Create HttpEntity using the helper method
        HttpEntity<TaskDTO> entity = createEntity(taskToCreate);

        try {
            restTemplate.exchange(createTaskUrl, HttpMethod.POST, entity, TaskDTO.class);
            throw new RuntimeException("Forcing Rollback");
        } catch (Exception e) {
            System.out.println("Exception triggered, transaction should be rolled back.");
        }
    }
}