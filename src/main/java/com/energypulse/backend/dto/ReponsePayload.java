package com.energypulse.backend.dto;

import org.springframework.http.HttpStatus;

import lombok.Builder;
import lombok.Data;

@Builder 
@Data 
public class ReponsePayload {
    private HttpStatus status;
    private Object data;
    private String message;
}
