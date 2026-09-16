package com.energypulse.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    private String hvacType;
    private String householdOccupants;
    private String name;
    private String username;
    private String email;
    private String isAgreedToTerms;
}
