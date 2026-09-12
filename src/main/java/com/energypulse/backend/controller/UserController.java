package com.energypulse.backend.controller;

import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.energypulse.backend.dto.ReponsePayload;
import com.energypulse.backend.dto.SignupDto;
import com.energypulse.backend.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController 
@RequiredArgsConstructor 
@Slf4j 
@RequestMapping ("/users/v1")
public class UserController {

    private final UserService userService;

    @PostMapping ("/signup")
    public ResponseEntity<?> createUserAccount(@RequestBody SignupDto signupDto) {
            ReponsePayload payload = userService.createUserAccount(signupDto);
            return ResponseEntity.ok(payload);
    }
}
