package com.example.SocialMedia.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRequest {
    private String content;
    private String postTopic;
    private String location;
}
