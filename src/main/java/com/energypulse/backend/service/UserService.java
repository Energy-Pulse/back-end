package com.energypulse.backend.service;

import com.energypulse.backend.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.energypulse.backend.model.User;
import com.energypulse.backend.repository.UserRepository;
import com.energypulse.backend.security.JwtService;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor  
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateCurrentUser(
            Authentication authentication,
            UpdateUserRequest request
    ) {

        User user = getAuthenticatedUser(authentication);

        if (request.getHvacType() != null) {
            user.setHvacType(request.getHvacType());
        }

        if (request.getHouseholdOccupants() != null) {
            user.setHouseholdOccupants(request.getHouseholdOccupants());
        }

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if (request.getIsAgreedToTerms() != null) {
            user.setIsAgreedToTerms(request.getIsAgreedToTerms());
        }

        user.setUpdatedAt(java.time.Instant.now());

        User updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }

    private User getAuthenticatedUser(Authentication authentication) {

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found")
                );
    }

    private UserResponse mapToResponse(User user) {

        return UserResponse.builder()
                .userId(user.getUserId())
                .hvacType(user.getHvacType())
                .householdOccupants(user.getHouseholdOccupants())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .isAgreedToTerms(user.getIsAgreedToTerms())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public ReponsePayload createUserAccount(SignupDto signupDto) {

        //  check aleady exists email
        if (userRepository.existsByEmail(signupDto.getEmail())) {
            return ReponsePayload.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Email already exists.")
                    .build();
        }

        User user = User.builder()
                .name(signupDto.getFirstName() + " " + signupDto.getLastName())
                .email(signupDto.getEmail())
                .password(passwordEncoder.encode(signupDto.getPassword()))
                .hvacType(signupDto.getHvacType())
                .householdOccupants(signupDto.getHouseholdOccupants())
                .isAgreedToTerms(signupDto.getIsAgreedToTerms())
                .username("@"+signupDto.getFirstName().toLowerCase()+signupDto.getLastName().toLowerCase())
                .build();

        userRepository.save(user);
        return ReponsePayload.builder()
        .status(HttpStatus.CREATED)
        .data(user)
        .message("User account created successfully.")
        .build();
    }

         public LoginResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password")
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPassword(),
                        java.util.Collections.emptyList()
                )
        );

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .message("Login successful")
                .build();
    
    }


}
