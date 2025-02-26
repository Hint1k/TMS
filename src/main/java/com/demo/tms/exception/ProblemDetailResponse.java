package com.demo.tms.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProblemDetailResponse {
    private String type;
    private int status;
    private String title;
    private String detail;
    private String instance;
}