package com.energypulse.backend.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@AllArgsConstructor 
@NoArgsConstructor 
@Data 
@Builder 
@Table (name = "users")
public class User {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID userId;
    private String hvacType;
    private String householdOccupants;
    private String name;
    private String username;
    private String email;
    private String contact;
    private String password;
    private String isAgreedToTerms;
    private Instant createdAt;
    private Instant updatedAt;
    private String status;

    @PrePersist 
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        userId = UUID.randomUUID();
        status = "ACTIVE";
    }
}
