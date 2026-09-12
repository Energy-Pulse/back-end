package com.energypulse.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ContactRequest {
    private String name;
    private String email;
    private String subject;
    private String message;
}
