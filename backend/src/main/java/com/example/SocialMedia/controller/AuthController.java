package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.RegisterRequest;
import com.example.SocialMedia.dto.RegisterResponse;
import com.example.SocialMedia.model.coredata_model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.SocialMedia.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
        User user = new User();
        user.setUserName(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());

        User savedUser = authService.register(user);

        RegisterResponse registerResponse = new RegisterResponse(savedUser.getId(), savedUser.getUsername(), "User registered successfully!", "", "");
        return new ResponseEntity<>(registerResponse, HttpStatus.CREATED);
    }
}