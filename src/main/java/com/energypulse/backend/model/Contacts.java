package com.energypulse.backend.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor 
@NoArgsConstructor 
@Data 
@Entity 
public class Contacts {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    
    private UUID externalId;
    private String name;
    private String email;
    private String subject;
    private String message; 
    private Instant createdAt;

    @PrePersist 
    public void prePersist() {
        this.externalId = UUID.randomUUID();
        this.createdAt = Instant.now();
    }
}
