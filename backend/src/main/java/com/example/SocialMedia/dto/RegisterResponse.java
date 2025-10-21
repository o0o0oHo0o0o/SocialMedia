package com.example.SocialMedia.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private int id;
    private String username;
    private String message;
    private String token;
    private String refreshToken;
}
