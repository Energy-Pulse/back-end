package com.energypulse.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.energypulse.backend.dto.ContactRequest;
import com.energypulse.backend.dto.ReponsePayload;
import com.energypulse.backend.service.ContactService;

import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@RequestMapping ("/contacts")
public class ContactController {

    private final ContactService contactService;

    @PostMapping ("/submit")
    public ResponseEntity<?> submitContactForm(@RequestBody ContactRequest contactRequest) {
        // Call the service to handle the contact form submission
        ReponsePayload payload = contactService.submitContactForm(contactRequest);
        return ResponseEntity.ok(payload);
    }
}
