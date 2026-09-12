package com.energypulse.backend.service;

import org.springframework.stereotype.Service;

import com.energypulse.backend.dto.ContactRequest;
import com.energypulse.backend.dto.ReponsePayload;
import com.energypulse.backend.model.Contacts;
import com.energypulse.backend.repository.ContactRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ContactService {
    private final ContactRepository contactRepository;

    public ReponsePayload submitContactForm(ContactRequest contactRequest) {
        // Create a new Contacts entity from the ContactRequest DTO
        Contacts contact = new Contacts();
        contact.setName(contactRequest.getName());
        contact.setEmail(contactRequest.getEmail());
        contact.setSubject(contactRequest.getSubject());
        contact.setMessage(contactRequest.getMessage());

        // Save the contact to the database
        Contacts savedContact = contactRepository.save(contact);

        // Create a response payload
       return ReponsePayload.builder()
                .message("Contact form submitted successfully.")
                .data(savedContact)
                .build();
    }
}
