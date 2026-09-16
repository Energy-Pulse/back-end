package com.energypulse.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID userId;
    private String hvacType;
    private String householdOccupants;
    private String name;
    private String username;
    private String email;
    private String isAgreedToTerms;
    private Instant createdAt;
    private Instant updatedAt;
}
