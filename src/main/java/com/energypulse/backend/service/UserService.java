package com.energypulse.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.energypulse.backend.dto.LoginRequest;
import com.energypulse.backend.dto.LoginResponse;
import com.energypulse.backend.dto.ReponsePayload;
import com.energypulse.backend.dto.SignupDto;
import com.energypulse.backend.model.User;
import com.energypulse.backend.repository.UserRepository;
import com.energypulse.backend.security.JwtService;

import lombok.RequiredArgsConstructor;

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
