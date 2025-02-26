package com.demo.tms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * {@code PagedResponseDTO} is a Data Transfer Object (DTO) used to represent paginated responses.
 * It encapsulates a list of data elements (content), along with pagination information, such as
 * the current page number, page size, total number of elements, and total number of pages.
 *
 * @param <T> The type of the content in the paginated response, typically a DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponseDTO<T> {

    /**
     * The content of the paginated response, which is a list of elements of type {@code T}.
     * This represents the actual data being returned.
     */
    private List<T> content;

    /**
     * The current page number in the paginated response.
     * The value is zero-based, meaning the first page is represented by 0.
     */
    private int pageNumber;

    /**
     * The size of each page in the paginated response.
     * Represents the number of items per page.
     */
    private int pageSize;

    /**
     * The total number of elements available across all pages.
     * This represents the total count of the items before pagination.
     */
    private long totalElements;

    /**
     * The total number of pages available in the paginated response.
     * This value is derived from the total number of elements and the page size.
     */
    private int totalPages;
}