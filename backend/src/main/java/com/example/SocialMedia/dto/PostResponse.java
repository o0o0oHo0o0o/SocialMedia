package com.example.SocialMedia.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostResponse {
    private int id;
    private String content;
    private String postTopic;
    private String location;
    private String username; // from User
    private LocalDateTime createdAt;
}
