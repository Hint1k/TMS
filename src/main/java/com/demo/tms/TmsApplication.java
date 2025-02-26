package com.demo.tms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.retry.annotation.EnableRetry;

/**
 * The main entry point for the Task Management System (TMS) application.
 * <p>
 * This class serves as the bootstrap for the Spring Boot application. It enables key features such as caching,
 * retry logic, and Spring Data web support to enhance the functionality of the application.
 * </p>
 *
 * <ul>
 *     <li>{@code @SpringBootApplication}: Indicates that this is a Spring Boot application.</li>
 *     <li>{@code @EnableRetry}: Enables retry functionality for methods that may require automatic
 *     retries on failures.</li>
 *     <li>{@code @EnableCaching}: Enables caching functionality in the application to improve performance by
 *     caching data.</li>
 *     <li>{@code @EnableSpringDataWebSupport}: Allows pagination and sorting of data in REST endpoints.</li>
 * </ul>
 */
@SpringBootApplication
@EnableRetry
@EnableCaching
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class TmsApplication {

    /**
     * The main method that runs the Task Management System application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(TmsApplication.class, args);
    }
}