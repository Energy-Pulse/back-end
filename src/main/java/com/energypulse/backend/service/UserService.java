package com.energypulse.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.energypulse.backend.dto.ReponsePayload;
import com.energypulse.backend.dto.SignupDto;
import com.energypulse.backend.model.User;
import com.energypulse.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor  
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ReponsePayload createUserAccount(SignupDto signupDto) {

        //  check aleady exists email
        if (userRepository.existsByEmail(signupDto.getEmail())) {
            throw new RuntimeException("Email already exists.");
        }

        User user = User.builder()
                .name(signupDto.getFirstName() + " " + signupDto.getLastName())
                .email(signupDto.getEmail())
                .password(passwordEncoder.encode(signupDto.getPassword()))
                .HVACType(signupDto.getHVACType())
                .householdOccupants(signupDto.getHouseholdOccupants())
                .isAgreedToTerms(signupDto.getIsAgreedToTerms())
                .username(signupDto.getFirstName()+signupDto.getLastName())
                .build();

        userRepository.save(user);
        return ReponsePayload.builder()
        .status(HttpStatus.CREATED)
        .data(user)
        .message("User account created successfully.")
        .build();
    }


}
