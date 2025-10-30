package com.example.SocialMedia.dto;

import com.example.SocialMedia.model.coredata_model.PostMedia;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PostResponse {
    private int id;
    private String content;
    private String postTopic;
    private String location;
    private String username; // from User
    private int reactionCount;
    private int commentCount;
    private int shareCount;
    private List<PostMediaResponse> medias;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}
