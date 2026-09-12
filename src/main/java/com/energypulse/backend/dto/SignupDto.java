package com.energypulse.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor 
@NoArgsConstructor 
@Data 
public class SignupDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String hvacType;
    private String householdOccupants;
    private String isAgreedToTerms;
}
 