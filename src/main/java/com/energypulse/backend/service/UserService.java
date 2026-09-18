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

import java.util.UUID;

@Service
@RequiredArgsConstructor  
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;



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

                if (!user.getStatus().equals("ACTIVE")) {
                    throw new RuntimeException("User account is not active");
                }

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


    public UUID getUserIdByEmail(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElse(null);
            return user != null ? user.getUserId() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ReponsePayload getUserDetails(UUID userId) {

        try {

            User user = userRepository.findByUserId(userId);

            if (user == null) {
                return ReponsePayload.builder()
                        .status(HttpStatus.NOT_FOUND)
                        .message("User not found.")
                        .build();
            }

            UserResponse userResponse = UserResponse.builder()
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

            return ReponsePayload.builder()
                    .status(HttpStatus.OK)
                    .data(userResponse)
                    .message("User details retrieved successfully.")
                    .build();

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }

    public ReponsePayload deleteUserAccount(UUID id) {
        
        try {
            User user = userRepository.findByUserId(id);

            if (user == null) {
                return ReponsePayload.builder()
                        .status(HttpStatus.NOT_FOUND)
                        .message("User not found.")
                        .build();
            }
            user.setStatus("DELETED");
            userRepository.save(user);

            return ReponsePayload.builder()
                    .status(HttpStatus.OK)
                    .message("User account deleted successfully.")
                    .build();

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }

    public ReponsePayload updateUserAccount(UUID id, UpdateUserRequest updateUserRequest) {
        
        try {
            User user = userRepository.findByUserId(id);

            if (user == null) {
                return ReponsePayload.builder()
                        .status(HttpStatus.NOT_FOUND)
                        .message("User not found.")
                        .build();
            }

            // Update the user fields with the new values from updateUserRequest
            user.setName(updateUserRequest.getName());
            user.setUsername(updateUserRequest.getUsername());
            user.setEmail(updateUserRequest.getEmail());
            user.setContact(updateUserRequest.getContact());

            // Save the updated user back to the repository
            userRepository.save(user);

            return ReponsePayload.builder()
                    .status(HttpStatus.OK)
                    .message("User account updated successfully.")
                    .build();

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }
}
