package com.energypulse.backend.controller;

import com.energypulse.backend.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.energypulse.backend.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
@RequestMapping("/users/v1")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> createUserAccount(@RequestBody SignupDto signupDto) {
            ReponsePayload payload = userService.createUserAccount(signupDto);
            return ResponseEntity.ok(payload);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        LoginResponse payload = userService.login(loginRequest);
        return ResponseEntity.ok(payload);
    }

}
