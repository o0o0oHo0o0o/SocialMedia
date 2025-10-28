package com.example.SocialMedia.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostRequest {
    private Integer postId;
    private String content;
    private String postTopic;
    private String location;
    private Integer userID;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}
