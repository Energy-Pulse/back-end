package com.energypulse.backend.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data 
@Builder 
@AllArgsConstructor 
public class LoginResponse {

    private String token;

    private String tokenType;

    private UUID userId;

    private String message;
}
